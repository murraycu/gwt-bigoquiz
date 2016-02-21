package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.Key;
import com.googlecode.objectify.VoidWork;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.shared.dto.UserRecentHistory;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.*;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings({"serial", "unchecked"})
public class QuizServiceImpl extends ServiceWithUser implements
        QuizService {
    private static final String LOADED_QUIZZES = "loaded-quizzes";

    @Nullable
    private Map<String, Quiz> quizzes = null;

    @Nullable
    @Override
    public List<Quiz.QuizDetails> getQuizList() throws IllegalArgumentException {
        getOrLoadQuizzes();

        if (quizzes == null) {
            return null;
        }

        final List<Quiz.QuizDetails> result = new ArrayList<>();
        for (final Quiz quiz : quizzes.values()) {
            if (quiz == null) {
                continue;
            }

            result.add(quiz.getDetails());
        }

        return result;
    }

    @NotNull
    @Override
    public  Quiz getQuiz(final String quizId) throws IllegalArgumentException {
        getOrLoadQuizzes();

        if (quizzes == null) {
            throw new IllegalArgumentException("Unknown quiz ID.");
        }

        final Quiz result = quizzes.get(quizId);
        if (result == null) {
            throw new IllegalArgumentException("Unknown quiz ID.");
        }

        return result;
    }

    private void getOrLoadQuizzes() {
        if (quizzes != null) {
            return;
        }

        final ServletConfig config = this.getServletConfig();
        if (config == null) {
            throw new RuntimeException("getServletConfig() returned null.");
        }

        final ServletContext context = config.getServletContext();
        if (context == null) {
            throw new RuntimeException("getServletContext() returned null.");
        }

        //Use the existing shared quiz if any:
        final Object object = context.getAttribute(LOADED_QUIZZES);
        if ((object != null) && !(object instanceof HashMap)) {
            throw new RuntimeException("he loaded-quizzes attribute is not of the expected type.");
        }

        quizzes = (HashMap<String, Quiz>)object;
        if (quizzes == null) {
            loadQuizzes(context);
            if (quizzes == null) {
                throw new IllegalArgumentException("No quizzes are available.");
            }
        }
    }

    @NotNull
    @Override
    public Question getQuestion(@NotNull final String quizId, @NotNull final String questionId) throws IllegalArgumentException {
        @NotNull final Quiz quiz = getQuiz(quizId);
        final Question result = quiz.getQuestion(questionId);
        if (result == null) {
            throw new IllegalArgumentException("Unknown question ID");
        }

        return result;
    }

    @Nullable
    @Override
    public Question getNextQuestion(@NotNull final String quizId, final String sectionId) throws IllegalArgumentException {
        @NotNull final Quiz quiz = getQuiz(quizId);

        @Nullable final Key<UserProfile> userKey = getUserKey();
        if (userKey == null) {
            //The user is not logged in,
            //so just return a random question:
            return quiz.getRandomQuestion(sectionId);
        }

        if (StringUtils.isEmpty(sectionId)) {
            @NotNull final Map<String, UserStats> mapUserStats = getUserStats(userKey, quizId);
            return getNextQuestionFromUserStats(null, quiz, mapUserStats);
        } else {
            //This special case is a bit copy-and-pasty of the general case with the
            //map, but it seems more efficient to avoid an unnecessary Map.
            @Nullable final UserStats userStats = getUserStatsForSection(userKey, sectionId, quizId);
            return getNextQuestionFromUserStatsForSection(sectionId, quiz, userStats);
        }
    }

    @NotNull
    @Override
    public QuizSections getSections(final String quizId) throws IllegalArgumentException {
        @NotNull final Quiz quiz = getQuiz(quizId);
        return quiz.getSections();
    }

    @NotNull
    @Override
    public SubmissionResult submitAnswer(final String quizId, final String questionId, final String answer, final boolean exact, final String nextQuestionSectionId) throws IllegalArgumentException {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(quizId, questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        final String correctAnswer = questionAndAnswer.getAnswer();
        if (correctAnswer == null) {
            throw new RuntimeException("submitAnswer(): correctAnswer was null.");
        }

        final boolean result = QuizUtils.answerIsCorrect(answer, correctAnswer, exact);

        return storeAnswerCorrectnessAndGetSubmissionResult(quizId, questionId, nextQuestionSectionId, questionAndAnswer, result);
    }

    @NotNull
    @Override
    public SubmissionResult submitDontKnowAnswer(final String quizId, final String questionId, final String nextQuestionSectionId) throws IllegalArgumentException {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(quizId, questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        //Store this like a don't know answer:
        return storeAnswerCorrectnessAndGetSubmissionResult(quizId, questionId, nextQuestionSectionId, questionAndAnswer, false);
    }

    @Nullable
    private QuestionAndAnswer getQuestionAndAnswer(final String quizId, final String questionId) {
        @NotNull final Quiz quiz = getQuiz(quizId);
        return quiz.getQuestionAndAnswer(questionId);
    }

    @Nullable
    @Override
    public UserRecentHistory getUserRecentHistory(final String quizId, final String requestUri) throws IllegalArgumentException {
        @NotNull final Quiz quiz = getQuiz(quizId);
        @NotNull final QuizSections sections = quiz.getSections();
        if (sections == null) {
            return null;
        }

        //Get the stats for this user, for each section:
        //We also return the LoginInfo, so we can show a sign in link,
        //and to avoid the need for a separate call to the server.
        @NotNull LoginInfo loginInfo = getLoginInfo(requestUri);
        @NotNull final UserRecentHistory result = new UserRecentHistory(loginInfo, sections);

        //This may be null,
        //in which case we will return a mostly-empty set of user statistics,
        //just to show what is possible when the user is logged in:
        @Nullable final String userId = loginInfo.getUserId();
        Key<UserProfile> userKey = null;

        @Nullable Map<String, UserStats> mapUserStats = null;
        if (!StringUtils.isEmpty(userId)) {
            userKey = Key.create(UserProfile.class, userId);
            mapUserStats = getUserStats(userKey, quizId);
        }

        for (final String sectionId : sections.getSectionIds()) {
            if (StringUtils.isEmpty(sectionId)) {
                //This seems wise.
                continue;
            }

            @Nullable UserStats userStats = null;
            if (mapUserStats != null) {
                userStats = mapUserStats.get(sectionId);
            }

            if (userStats == null) {
                //So we get the default values:
                userStats = new UserStats(userKey, quizId, sectionId);
            }

            //Set the titles.
            //We don't store these in the datastore because we can get them easily from the Quiz.
            //TODO: It might really be more efficient to store them in the datastore.
            for (@NotNull final UserQuestionHistory userQuestionHistory : userStats.getQuestionHistories()) {
                @Nullable final Question question = quiz.getQuestion(userQuestionHistory.getQuestionId());
                if (question != null) {
                    userQuestionHistory.setQuestionTitle(question.getText());
                }

                @Nullable final String subSectionTitle = sections.getSubSectionTitle(question.getSectionId(),
                        question.getSubSectionId());
                userQuestionHistory.setSubSectionTitle(subSectionTitle);
            }

            result.setSectionStats(sectionId, userStats.getDetails());
        }

        return result;
    }

    @Override
    public void resetSections(final String quizId) {
        @Nullable final Key<UserProfile> userKey = getUserKey();
        if (userKey == null) {
            Log.error("resetSections(): userKey was null.");return;
        }

        if (StringUtils.isEmpty(quizId)) {
            throw new IllegalArgumentException("Empty or null quiz ID.");
        }

        //TODO: Get the keys only:
        final Query q = getQueryForUserStats(userKey, quizId);
        final List<UserStats> list = q.list();
        if (list.isEmpty()) {
            //Presumably, they don't exist yet, or have already been deleted.
            return;
        }

        for (final UserStats userStats : list) {
            //TODO: Batch these:
            EntityManagerFactory.ofy().delete().entity(userStats).now();
        }
    }

    private static Quiz loadQuiz(@NotNull final String quizId) {
        final String filename = quizId + ".xml";
        try(final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filename)) {
            if (is == null) {
                Log.fatal("quiz XML fie not found: " + filename);
                return null;
            }

            try {
                return QuizLoader.loadQuiz(is);
            } catch (final QuizLoader.QuizLoaderException e) {
                Log.fatal("loadQuiz() failed", e);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    private UserStats getUserStatsForSection(@NotNull final Key<UserProfile> userKey, @NotNull final String quizId, @NotNull final String sectionId) {
        Query<UserStats> q = getQueryForUserStats(userKey, quizId);
        q = q.filter("details.sectionId", sectionId);
        q = q.limit(1);
        final List<UserStats> list = q.list();
        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Get a map of section ID to UserStats for that section, for the specified user.
     *
     * @param userId
     * @return
     */
    @NotNull
    private Map<String, UserStats> getUserStats(@NotNull final Key<UserProfile> userId, @NotNull final String quizId) {
        final Query<UserStats> q = getQueryForUserStats(userId, quizId);

        @NotNull final Map<String, UserStats> map = new HashMap<>();
        for (@NotNull final UserStats userStats : q.list()) {
            map.put(userStats.getSectionId(), userStats);
        }

        return map;
    }

    private static Query<UserStats> getQueryForUserStats(@NotNull final Key<UserProfile> userKey, @NotNull final String quizId) {
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.ancestor(userKey);
        q = q.filter("details.quizId", quizId);
        return q;
    }

    private UserProfile getUserProfileImpl() {
        @Nullable final User user = getUser();
        if (user == null) {
            return null;
        }

        return getUserProfileFromDataStore(user);
    }

    private void loadQuizzes(@NotNull final ServletContext context) {
        final Map<String, Quiz> quizzes = new HashMap<>();

        //Load it for the first time:
        if (loadQuizIntoQuizzes(QuizConstants.DEFAULT_QUIZ_ID, quizzes)) {
            return;
        }

        if (loadQuizIntoQuizzes("mastermethodquiz", quizzes)) {
            return;
        }

        this.quizzes = quizzes;
        context.setAttribute(LOADED_QUIZZES, quizzes);
    }

    private boolean loadQuizIntoQuizzes(final String quizId, Map<String, Quiz> quizzes) {
        if (quizzes.containsKey(quizId)) {
            Log.error("loadQuizIntoQuizzes(): quiz already loaded: " + quizId);
        }

        final Quiz quiz;
        try {
            quiz = loadQuiz(quizId);
            if (quiz != null) {
                quizzes.put(quizId, quiz);
            }
        } catch (@NotNull final Exception e) {
            Log.error("Could not load quiz: " + quizId, e);
            return true;
        }
        return false;
    }

    @NotNull
    private SubmissionResult createSubmissionResult(boolean result, final String quizId, @NotNull final QuestionAndAnswer questionAndAnswer, final Key<UserProfile> userId, @Nullable final String nextQuestionSectionId) {
        @NotNull final Quiz quiz = getQuiz(quizId);

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable String correctAnswer = null;
        if (!result) {
            correctAnswer = questionAndAnswer.getAnswer();
        }

        @Nullable final Question nextQuestion;
        if (nextQuestionSectionId != null) {
            @Nullable final UserStats userStats = getUserStatsForSection(userId, quizId, nextQuestionSectionId);
            nextQuestion = getNextQuestionFromUserStatsForSection(nextQuestionSectionId, quiz, userStats);
        } else {
            @Nullable final Map<String, UserStats> mapUserStats = getUserStats(userId, quizId);
            nextQuestion = getNextQuestionFromUserStats(nextQuestionSectionId, quiz, mapUserStats);
        }

        return new SubmissionResult(result, correctAnswer, nextQuestion);
    }

    private void storeAnswerForSectionInTransaction(final boolean result, @NotNull final String quizId, @Nullable final Question question, final Key<UserProfile> userKey) {
        EntityManagerFactory.ofy().transact(new VoidWork() {
            public void vrun() {
                if (question == null) {
                    Log.error("storeAnswerForSection(): question is null.");
                    return;
                }

                final String sectionId = question.getSectionId();
                if (StringUtils.isEmpty(sectionId)) {
                    Log.error("storeAnswerForSection(): question's sectionId is null.");
                    return;
                }

                if (userKey == null) {
                    Log.error("setUserStatsImpl(): userKey is null.");
                    return;
                }

                //TODO: Combine the rest in a transaction:
                @Nullable UserStats userStats = getUserStatsForSection(userKey, quizId, sectionId);

                //Create new UserStats if necessary,
                //for instance if this is the first time storing an answer for this section.
                if (userStats == null) {
                    userStats = new UserStats(userKey, quizId, sectionId);
                }

                userStats.incrementAnswered();

                if (result) {
                    userStats.incrementCorrect();
                }

                userStats.updateProblemQuestion(question, result);

                EntityManagerFactory.ofy().save().entity(userStats).now();
            }
        });
    }

    /*
    private static String getCurrentTime() {
        //TODO: Performance:
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        df.setTimeZone(tz);
        return df.format(new Date());
    }
    */

    /**
     *
     * @return null if the user is not logged in.
     */
    private String getUserId() {
        @Nullable final User user = getUser();
        if (user == null) {
            return null;
        }

        return user.getUserId();
    }

    /**
     *
     * @return null if the user is not logged in.
     */
    private Key<UserProfile> getUserKey() {
        final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            return null;
        }

        return Key.create(UserProfile.class, userId);
    }

    @NotNull
    private SubmissionResult storeAnswerCorrectnessAndGetSubmissionResult(final String quizId, final String questionId, final String nextQuestionSectionId, @NotNull final QuestionAndAnswer questionAndAnswer, boolean result) {
        //If the user is logged in, store whether we got the question right or wrong:
        @Nullable final Key<UserProfile> userKey = getUserKey();

        // Store the answer.
        // Because this happens asynchronously, in a transaction,
        // we cannot reuse the read of the UserStats for both updating the UserStats and choosing the next question.
        storeAnswerForSectionInTransaction(result, quizId, questionAndAnswer.getQuestion(), userKey);

        return createSubmissionResult(result, quizId, questionAndAnswer, userKey, nextQuestionSectionId);
    }

    /**
     *
     * @param sectionId
     * @param quiz
     * @param userStats Can be null.
     * @return
     */
    private Question getNextQuestionFromUserStatsForSection(@NotNull final String sectionId, @NotNull final Quiz quiz, @Nullable final UserStats userStats) {
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("getNextQuestionFromPerSectionUserStat(): sectionId was null.");
            return null;
        }

        //TODO: Avoid this temporary map:
        @NotNull final Map<String, UserStats> map = new HashMap<>();
        if (userStats != null) {
            map.put(userStats.getSectionId(), userStats);
        }

        return getNextQuestionFromUserStats(sectionId, quiz, map);
    }

    @Nullable
    private Question getNextQuestionFromUserStats(@Nullable final String sectionId, @NotNull final Quiz quiz, @Nullable final Map<String, UserStats> mapUserStats) {
        final int MAX_TRIES = 10;
        int tries = 0;
        @Nullable Question question = null;
        @Nullable Question questionBestSoFar = null;
        int questionBestCountAnsweredWrong = 0;
        while(tries < MAX_TRIES) {
            tries += 1;

            question = quiz.getRandomQuestion(sectionId);
            if (question == null) {
                continue;
            }

            if (questionBestSoFar == null) {
                questionBestSoFar = question;
            }

            if (mapUserStats == null) {
                //Assume this means the user has never answered any question in any section.
                return question;
            }

            final UserStats userStats = mapUserStats.get(question.getSectionId());
            if (userStats == null) {
                //Assume this means the user has never answered any question in the section.
                return question;
            }

            final String questionId = question.getId();

            //Prioritize questions that have never been asked.
            if (!userStats.getQuestionWasAnswered(questionId)) {
                return question;
            }

            //Otherwise, try a few times to get a question that
            //we have got wrong many times:
            //We could just get the most-wrong answer directly,
            //but we want some randomness.
            final int countAnsweredWrong = userStats.getQuestionCountAnsweredWrong(questionId);
            if (countAnsweredWrong > questionBestCountAnsweredWrong) {
                questionBestSoFar = question;
                questionBestCountAnsweredWrong = countAnsweredWrong;
            }
        }

        return questionBestSoFar;
    }
}

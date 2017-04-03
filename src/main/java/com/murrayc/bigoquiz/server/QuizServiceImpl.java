package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.*;
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
import java.io.File;
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


    private static class QuizzesMap {
        @Nullable
        public final Map<String, Quiz> map = new HashMap<>();
        public boolean allTitlesLoaded = false;
    }

    private QuizzesMap quizzes = null;

    @Nullable
    @Override
    public List<Quiz.QuizDetails> getQuizList() throws IllegalArgumentException {
        getOrLoadQuizzes();

        if (quizzes == null) {
            return null;
        }

        //TODO: Cache this.
        final List<Quiz.QuizDetails> result = new ArrayList<>();
        for (final Quiz quiz : quizzes.map.values()) {
            if (quiz == null) {
                continue;
            }

            result.add(quiz.getDetails());
        }

        Collections.sort(result,
                HasIdAndTitle.generateTitleSortComparator());

        return result;
    }

    @NotNull
    @Override
    public  Quiz getQuiz(final String quizId)  throws UnknownQuizException, IllegalArgumentException {
        if (!loadQuizIntoQuizzes(quizId)) {
            throw new UnknownQuizException();
        }

        if (quizzes == null) {
            throw new UnknownQuizException();
        }

        final Quiz result = quizzes.map.get(quizId);
        if (result == null) {
            throw new UnknownQuizException();
        }

        return result;
    }

    private void getQuizzesMap() {
        final ServletConfig config = this.getServletConfig();
        if (config == null) {
            throw new RuntimeException("getServletConfig() returned null.");
        }

        final ServletContext context = config.getServletContext();
        if (context == null) {
            throw new RuntimeException("getServletContext() returned null.");
        }

        //Use the existing shared quizzes if any:
        final Object object = context.getAttribute(LOADED_QUIZZES);
        if ((object != null) && !(object instanceof QuizzesMap)) {
            throw new RuntimeException("The loaded-quizzes attribute is not of the expected type.");
        }

        quizzes = (QuizzesMap) object;
        if (quizzes == null) {
            quizzes = new QuizzesMap();
            context.setAttribute(LOADED_QUIZZES, quizzes);
        }
    }

    private void getOrLoadQuizzes() {
        // Load all quizzes.
        getQuizzesMap();

        if (quizzes.allTitlesLoaded) {
            return;
        }

        final String[] names = {
                QuizConstants.DEFAULT_QUIZ_ID,
                "algorithms_analysis",
                "designpatterns",
                "graphs",
                "cpp_std_algorithms",
                "notation",
                "numbers",
                "algorithms",
                "string_algorithms",
                "combinatorics",
                "math",
                "datastructures",
                "bitwise",
                "concurrency",
                "distributed_systems",
                "book_stepanov_fmtgp",
                "networking"};

        for (final String name : names) {
            loadQuizIntoQuizzes(name, quizzes);
        }

        quizzes.allTitlesLoaded = true;
    }

    //TODO: This seems to be called unnecessarily right after getNextQuestion().
    @NotNull
    @Override
    public Question getQuestion(@NotNull final String quizId, @NotNull final String questionId) throws IllegalArgumentException {
        @NotNull final Quiz quiz = getQuiz(quizId);
        final Question result = quiz.getQuestion(questionId);
        if (result == null) {
            throw new IllegalArgumentException("Unknown question ID");
        }

        if (result != null) {
            setQuestionExtras(result, quiz);
        }

        if (!result.hasChoices()) {
            // This would be OK if multiple-choice should not be used with this choice.
            Log.error("getQuestion(): The result has no answer choices: " + result.getId());
        }

        return result;
    }

    @Nullable
    @Override
    public Question getNextQuestion(@NotNull final String quizId, final String sectionId) throws IllegalArgumentException {
        @NotNull final Quiz quiz = getQuiz(quizId);

        Question result = null;

        @Nullable final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            //The user is not logged in,
            //so just return a random question:
            result = quiz.getRandomQuestion(sectionId);
        } else if (StringUtils.isEmpty(sectionId)) {
            @NotNull final Map<String, UserStats> mapUserStats = getUserStats(userId, quizId);
            result = getNextQuestionFromUserStats(null, quiz, mapUserStats);
        } else {
            //This special case is a bit copy-and-pasty of the general case with the
            //map, but it seems more efficient to avoid an unnecessary Map.
            @Nullable final UserStats userStats = getUserStatsForSection(userId, sectionId, quizId);
            result = getNextQuestionFromUserStatsForSection(sectionId, quiz, userStats);
        }

        if (result != null) {
            setQuestionExtras(result, quiz);
        }

        return result;
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

        final Question.Text correctAnswer = questionAndAnswer.getAnswer();
        if (correctAnswer == null) {
            throw new RuntimeException("submitAnswer(): correctAnswer was null.");
        }

        final boolean result = QuizUtils.answerIsCorrect(answer, correctAnswer.text, exact);

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
    public UserHistorySections getUserHistorySections(final String quizId, final String requestUri) throws UnknownQuizException, IllegalArgumentException {
        final Quiz quiz = getQuiz(quizId);
        if (quiz == null) {
            throw new UnknownQuizException();
        }

        @NotNull final QuizSections sections = quiz.getSections();
        if (sections == null) {
            return null;
        }

        //Get the stats for this user, for each section:
        //We also return the LoginInfo, so we can show a sign in link,
        //and to avoid the need for a separate call to the server.
        @NotNull LoginInfo loginInfo = getLoginInfo(requestUri);
        @NotNull final UserHistorySections result = new UserHistorySections(loginInfo, sections, quiz.getTitle());

        //This may be null,
        //in which case we will return a mostly-empty set of user statistics,
        //just to show what is possible when the user is logged in:
        @Nullable final String userId = loginInfo.getUserId();

        @Nullable Map<String, UserStats> mapUserStats = null;
        if (!StringUtils.isEmpty(userId)) {
            mapUserStats = getUserStats(userId, quizId);
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
                userStats = new UserStats(userId, quizId, sectionId);
            }

            //Set the titles.
            //We don't store these in the datastore because we can get them easily from the Quiz.
            //TODO: It might really be more efficient to store them in the datastore.
            List<String> toRemove = null;
            for (@NotNull final UserQuestionHistory userQuestionHistory : userStats.getTopProblemQuestionHistories()) {
                final String questionId = userQuestionHistory.getQuestionId();
                @Nullable final Question question = quiz.getQuestion(questionId);

                //If the question history is invalid, remember that:
                if (question == null ||
                        !StringUtils.equals(question.getSectionId(), sectionId)) {
                    Log.error("question was null or in the wrong section for id:" + questionId);

                    if (toRemove == null) {
                        toRemove = new ArrayList<>();
                    }

                    toRemove.add(questionId);
                    continue;
                }

                userQuestionHistory.setQuestionTitle(question.getText());

                @Nullable final String subSectionTitle = sections.getSubSectionTitle(question.getSectionId(),
                        question.getSubSectionId());
                userQuestionHistory.setSubSectionTitle(subSectionTitle);
            }

            //Remove any invalid question histories:
            if (toRemove != null) {
                for (final String questionId : toRemove) {
                    userStats.removeQuestionHistory(questionId);
                }

                //Save it so we don't have to remove it next time:
                //TODO: This doesn't seem to work - we have to remove it again next time.
                EntityManagerFactory.ofy().save().entity(userStats).now();
            }

            result.setSectionStats(sectionId, userStats);
        }

        return result;
    }

    @Override
    public void resetSections(final String quizId) {
        @Nullable final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            //TODO: Throw some NotLoggedIn exception?
            Log.error("resetSections(): userId is null.");
            return;
        }

        if (StringUtils.isEmpty(quizId)) {
            throw new IllegalArgumentException("Empty or null quiz ID.");
        }

        //TODO: Get the keys only:
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        q = q.filter("quizId", quizId);
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

    @Nullable
    @Override
    public UserHistoryOverall getUserHistoryOverall(final String requestUri) throws IllegalArgumentException {
        getOrLoadQuizzes();

        //Get the stats for this user, for each section:
        //We also return the LoginInfo, so we can show a sign in link,
        //and to avoid the need for a separate call to the server.
        @NotNull LoginInfo loginInfo = getLoginInfo(requestUri); //TODO: Check for login

        @NotNull final UserHistoryOverall result = new UserHistoryOverall(loginInfo);

        @Nullable final String userId = loginInfo.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return result;
        }

        final Map<String, UserStats> mapUserStats = getUserStats(userId);

        for (final Quiz quiz : quizzes.map.values()) {
            final String quizId = quiz.getId();
            final UserStats userStats = mapUserStats.get(quizId);
            if (userStats == null) {
                continue;
            }

            result.setQuizStats(quizId, userStats, quiz.getTitle(), quiz.getQuestionsCount());
        }

        return result;
    }

    private static Quiz loadQuiz(@NotNull final String quizId) {
        final String filename = "quizzes" + File.separator + quizId + ".xml";
        try(final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(filename)) {
            if (is == null) {
                Log.fatal("quiz XML file not found: " + filename);
                return null;
            }

            try {
                return QuizLoader.loadQuiz(is);
            } catch (final QuizLoader.QuizLoaderException e) {
                Log.fatal("loadQuiz() failed", e);
            }
        } catch (final IOException e) {
            Log.error("loadQuiz(): Could not get file as stream from resouce", e);
        }

        return null;
    }

    private UserStats getUserStatsForSection(@NotNull final String userId, @NotNull final String quizId, @NotNull final String sectionId) {
        Query<UserStats> q = getQueryForUserStats(userId, quizId);
        q = q.filter("sectionId", sectionId);
        q = q.limit(1);
        final List<UserStats> list = q.list();
        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    /**
     * Get a map of quiz ID to UserStats for that quiz, for the specified user.
     *
     * @param userId
     * @return
     */
    @NotNull
    private Map<String, UserStats> getUserStats(@NotNull final String userId) {
        final Query<UserStats> q = getQueryForUserStats(userId);

        @NotNull List<UserStats> listUserStats = q.list();
        @NotNull final Map<String, UserStats> map = new HashMap<>();
        for (@NotNull final UserStats userStats : listUserStats) {
            final String quizId = userStats.getQuizId();
            if (!map.containsKey(quizId)) {
                userStats.makeSane();
                map.put(quizId, userStats);
            } else {
                final UserStats existing = map.get(quizId);
                final UserStats combinedStats = existing.createCombinedUserStatsWithoutQuestionHistories(userStats);
                combinedStats.makeSane();
                map.put(quizId, combinedStats);
            }
        }

        return map;
    }

    /**
     * Get a map of section ID to UserStats for that section, for the specified user.
     *
     * @param userId
     * @return
     */
    @NotNull
    private Map<String, UserStats> getUserStats(@NotNull final String userId, @NotNull final String quizId) {
        final Query<UserStats> q = getQueryForUserStats(userId, quizId);

        @NotNull final Map<String, UserStats> map = new HashMap<>();
        for (@NotNull final UserStats userStats : q.list()) {
            map.put(userStats.getSectionId(), userStats);
        }

        return map;
    }

    private static Query<UserStats> getQueryForUserStats(@NotNull final String userId) {
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        return q;
    }

    private static Query<UserStats> getQueryForUserStats(@NotNull final String userId, @NotNull final String quizId) {
        Query<UserStats> q = getQueryForUserStats(userId);
        q = q.filter("quizId", quizId);
        return q;
    }

    private UserProfile getUserProfileImpl() {
        @Nullable final User user = getUser();
        if (user == null) {
            return null;
        }

        return getUserProfileFromDataStore(user);
    }

    private boolean loadQuizIntoQuizzes(final String quizId) {
        getQuizzesMap();

        if (!quizzes.map.containsKey(quizId)) {
            if (!loadQuizIntoQuizzes(quizId, quizzes)) {
                Log.error("Could not load quiz: " + quizId);
                return false;
            }
        }

        return true;
    }

    /**
     * Returns false if the load failed.
     * @param quizId
     * @param quizzes
     * @return
     */
    private boolean loadQuizIntoQuizzes(final String quizId, final QuizzesMap quizzes) {
        if (quizzes.map.containsKey(quizId)) {
            Log.error("loadQuizIntoQuizzes(): quiz already loaded: " + quizId);
            return true;
        }

        final Quiz quiz;
        try {
            quiz = loadQuiz(quizId);
            if (quiz != null) {
                quizzes.map.put(quizId, quiz);
            }
        } catch (@NotNull final Exception e) {
            Log.error("Could not load quiz: " + quizId, e);
            return false;
        }

        return true;
    }

    @NotNull
    private SubmissionResult createSubmissionResult(boolean result, final String quizId, @NotNull final String questionId, @Nullable final String nextQuestionSectionId, @Nullable final Map<String, UserStats> mapUserStats) {
        @NotNull final Quiz quiz = getQuiz(quizId);

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable Question.Text correctAnswer = null;
        if (!result) {
            correctAnswer = quiz.getAnswer(questionId);
        }

        @Nullable final Question nextQuestion = getNextQuestionFromUserStats(nextQuestionSectionId, quiz, mapUserStats);
        return generateSubmissionResult(result, quiz, correctAnswer, nextQuestion);
    }

    @NotNull
    private SubmissionResult generateSubmissionResult(boolean result, Quiz quiz, Question.Text correctAnswer, Question nextQuestion) {
        setQuestionExtras(nextQuestion, quiz);
        return new SubmissionResult(result, correctAnswer, nextQuestion);
    }

    /**
     *
     * @param result
     * @param questionId
     * @param nextQuestionSectionId
     * @param userStats This may be null if nextQuestionSectionId is null.
     * @return
     */
    @NotNull
    private SubmissionResult createSubmissionResultForSection(boolean result, final String quizId, final String questionId, final String nextQuestionSectionId, final UserStats userStats) {
        @NotNull final Quiz quiz = getQuiz(quizId);

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable Question.Text correctAnswer = null;
        if (!result) {
            correctAnswer = quiz.getAnswer(questionId);
        }

        @Nullable final Question nextQuestion = getNextQuestionFromUserStatsForSection(nextQuestionSectionId, quiz, userStats);
        return generateSubmissionResult(result, quiz, correctAnswer, nextQuestion);
    }

    private void storeAnswer(boolean result, @NotNull final String quizId, @NotNull final Question question, final String userId, @Nullable final Map<String, UserStats> mapUserStats) {
        if (StringUtils.isEmpty(userId)) {
            Log.error("storeAnswer(): userId was null.");
            return;
        }

        if (mapUserStats == null) {
            Log.error("storeAnswer(): mapUserStats was null.");
            return;
        }

        //Update the statistics:
        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("storeAnswer(): sectionId is null.");
            return;
        }

        UserStats userStats = mapUserStats.get(sectionId);
        if (userStats == null) {
            userStats = new UserStats(userId, quizId, sectionId);
        }

        storeAnswerForSection(result, quizId, question, userId, userStats);
    }

    private void storeAnswerForSection(boolean result, @NotNull final String quizId, @Nullable final Question question, final String userId, @Nullable UserStats userStats) {
        if (question == null) {
            Log.error("storeAnswerForSection(): question is null.");
            return;
        }

        //Create new UserStats if necessary,
        //for instance if this is the first time storing an answer for this section.
        if (userStats == null) {
            if (StringUtils.isEmpty(userId)) {
                Log.error("storeAnswerForSection(): userId is empty.");
                return;
            }

            final String sectionId = question.getSectionId();
            if (StringUtils.isEmpty(sectionId)) {
                Log.error("storeAnswerForSection(): sectionId is empty.");
                return;
            }

            userStats = new UserStats(userId, quizId, sectionId);
        }

        userStats.incrementAnswered();

        if (result) {
            userStats.incrementCorrect();
        }

        userStats.updateProblemQuestion(question, result);

        EntityManagerFactory.ofy().save().entity(userStats).now();
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

    @NotNull
    private SubmissionResult storeAnswerCorrectnessAndGetSubmissionResult(final String quizId, final String questionId, final String nextQuestionSectionId, @NotNull final QuestionAndAnswer questionAndAnswer, boolean result) {
        //If the user is logged in, store whether we got the question right or wrong:
        @Nullable final String userId = getUserId();

        final String sectionId = questionAndAnswer.getQuestion().getSectionId();

        // Get the UserStats (or a map of them), and use it for both storing the answer and getting the next question,
        // to avoid getting the UserStats twice from the datastore.
        //
        // Call different methods depending on whether nextQuestionSectionId is specified an as is the same as the
        // questino's section ID, to avoid allocating a Map just containing one UserStats.
        if (!StringUtils.isEmpty(nextQuestionSectionId) &&
                StringUtils.equals(nextQuestionSectionId, sectionId)) {
            @Nullable UserStats userStats = null;
            if (!StringUtils.isEmpty(userId)) {
                userStats = getUserStatsForSection(userId, quizId, nextQuestionSectionId);
                storeAnswerForSection(result, quizId, questionAndAnswer.getQuestion(), userId, userStats);
            }

            return createSubmissionResultForSection(result, quizId, questionId, nextQuestionSectionId, userStats);
        } else {
            @Nullable Map<String, UserStats> mapUserStats = null;
            if (!StringUtils.isEmpty(userId)) {
                mapUserStats = getUserStats(userId, quizId);
                storeAnswer(result, quizId, questionAndAnswer.getQuestion(), userId, mapUserStats);
            }

            return createSubmissionResult(result, quizId, questionId, nextQuestionSectionId, mapUserStats);
        }
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

    private void setQuestionExtras(final Question question, @NotNull Quiz quiz) {
        String subSectionTitle = null;
        @NotNull final QuizSections sections = quiz.getSections();
        if (sections != null) {
            subSectionTitle = sections.getSubSectionTitle(question.getSectionId(),
                    question.getSubSectionId());
        }
        question.setTitles(quiz.getTitle(), subSectionTitle, question);

        question.setQuizUsesMathML(quiz.getUsesMathML());
    }
}

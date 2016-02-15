package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
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
@SuppressWarnings("serial")
public class QuizServiceImpl extends ServiceWithUser implements
        QuizService {
    private static final String LOADED_QUIZ = "loaded-quiz";

    /*
    public String greetServer(String input) throws IllegalArgumentException {
      // Verify that the input is valid.
      if (!FieldVerifier.isValidName(input)) {
        // If the input is not valid, throw an IllegalArgumentException back to
        // the client.
        throw new IllegalArgumentException(
            "Name must be at least 4 characters long");
      }

      String serverInfo = getServletContext().getServerInfo();
      String userAgent = getThreadLocalRequest().getHeader("UserProfile-Agent");

      // Escape data from the client to avoid cross-site script vulnerabilities.
      input = escapeHtml(input);
      userAgent = escapeHtml(userAgent);

      return "Hello, " + input + "!<br><br>I am running " + serverInfo
          + ".<br><br>It looks like you are using:<br>" + userAgent;
    }
    */
    @Nullable
    public Quiz quiz;

    public static Quiz loadQuiz() {
        try(final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml")) {
            if (is == null) {
                @NotNull final String errorMessage = "quiz.xml not found.";
                Log.fatal(errorMessage);
                return null;
            }

            return QuizLoader.loadQuiz(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    @Override
    public Question getQuestion(final String questionId) throws IllegalArgumentException {
        @Nullable final Quiz quiz = getQuiz();
        return quiz.getQuestion(questionId);
    }

    @Nullable
    @Override
    public Question getNextQuestion(final String sectionId) throws IllegalArgumentException {
        @Nullable final Quiz quiz = getQuiz();

        @Nullable final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            //The user is not logged in,
            //so just return a random question:
            return quiz.getRandomQuestion(sectionId);
        }

        if (StringUtils.isEmpty(sectionId)) {
            @NotNull final Map<String, UserStats> mapUserStats = getUserStats(userId);
            return getNextQuestionFromUserStats(null, quiz, mapUserStats);
        } else {
            //This special case is a bit copy-and-pasty of the general case with the
            //map, but it seems more efficient to avoid an unncessary Map.
            @Nullable final UserStats userStats = getUserStatsForSection(userId, sectionId);
            return getNextQuestionFromUserStatsForSection(sectionId, quiz, userStats);
        }
    }

    @NotNull
    @Override
    public QuizSections getSections() throws IllegalArgumentException {
        @Nullable final Quiz quiz = getQuiz();
        return quiz.getSections();
    }

    @NotNull
    @Override
    public SubmissionResult submitAnswer(final String questionId, final String answer, String nextQuestionSectionId) throws IllegalArgumentException {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        final boolean result = StringUtils.equals(questionAndAnswer.getAnswer(), answer);
        return storeAnswerCorrectnessAndGetSubmissionResult(questionId, nextQuestionSectionId, questionAndAnswer, result);
    }

    @NotNull
    @Override
    public SubmissionResult submitDontKnowAnswer(final String questionId, final String nextQuestionSectionId) throws IllegalArgumentException {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        //Store this like a don't know answer:
        return storeAnswerCorrectnessAndGetSubmissionResult(questionId, nextQuestionSectionId, questionAndAnswer, false);
    }

    @Nullable
    private QuestionAndAnswer getQuestionAndAnswer(final String questionId) {
        @Nullable final Quiz quiz = getQuiz();
        return quiz.getQuestionAndAnswer(questionId);
    }

    @Nullable
    @Override
    public UserRecentHistory getUserRecentHistory(final String requestUri) throws IllegalArgumentException {
        @Nullable final Quiz quiz = getQuiz();
        @NotNull final QuizSections sections = quiz.getSections();
        if (sections == null) {
            return null;
        }

        //Get the stats for this user, for each section:
        //We also return the LoginInfo, so we can show a sign in link,
        //and to avoid the need for a seperate call to the server.
        @NotNull LoginInfo loginInfo = getLoginInfo(requestUri);
        @NotNull final UserRecentHistory result = new UserRecentHistory(loginInfo, sections);

        //This may be null,
        //in which case we will return a mostly-empty set of user statistics,
        //just to show what is possible when the user is logged in:
        @Nullable final String userId = loginInfo.getUserId();

        @NotNull final Map<String, UserStats> mapUserStats = getUserStats(userId);
        for (final String sectionId : sections.getSectionIds()) {
            if (StringUtils.isEmpty(sectionId)) {
                //This seems wise.
                continue;
            }

            @Nullable UserStats userStats = mapUserStats.get(sectionId);
            if (userStats == null) {
                //So we get the default values:
                userStats = new UserStats(userId, sectionId);
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

            result.setSectionStats(sectionId, userStats);
        }

        return result;
    }

    @Override
    public void resetSections() {
        @Nullable final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            Log.error("resetSections(): userId was null.");
            return;
        }

        //TODO: Get the keys only:
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
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

    private UserStats getUserStatsForSection(final String userId, final String sectionId) {
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        q = q.filter("sectionId", sectionId);
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
    private Map<String, UserStats> getUserStats(final String userId) {
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);

        @NotNull final Map<String, UserStats> map = new HashMap<>();
        for (@NotNull final UserStats userStats : q.list()) {
            map.put(userStats.getSectionId(), userStats);
        }

        return map;
    }

    private UserProfile getUserProfileImpl() {
        @Nullable final User user = getUser();
        if (user == null) {
            return null;
        }

        return getUserProfileFromDataStore(user);
    }

    private Quiz getQuiz() {
        if (quiz != null) {
            return quiz;
        }

        final ServletConfig config = this.getServletConfig();
        if (config == null) {
            Log.error("getServletConfig() return null");
            return null;
        }

        final ServletContext context = config.getServletContext();
        if (context == null) {
            Log.error("getServletContext() return null");
            return null;
        }

        //Use the existing shared quiz if any:
        final Object object = context.getAttribute(LOADED_QUIZ);
        if ((object != null) && !(object instanceof Quiz)) {
            Log.error("The loaded-quiz attribute is not of the expected type.");
            return null;
        }

        quiz = (Quiz) object;
        if (quiz != null) {
            return quiz;
        }

        //Load it for the first time:
        try {
            quiz = loadQuiz();
        } catch (@NotNull final Exception e) {
            e.printStackTrace();
            return null;
        }

        context.setAttribute(LOADED_QUIZ, quiz);

        return quiz;
    }

    @NotNull
    private SubmissionResult createSubmissionResult(boolean result, @NotNull final String questionId, @Nullable final String nextQuestionSectionId, @Nullable final Map<String, UserStats> mapUserStats) {
        @Nullable final Quiz quiz = getQuiz();

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable String correctAnswer = null;
        if (!result) {
            correctAnswer = quiz.getAnswer(questionId);
        }

        @Nullable final Question nextQuestion = getNextQuestionFromUserStats(nextQuestionSectionId, quiz, mapUserStats);
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
    private SubmissionResult createSubmissionResultForSection(boolean result, final String questionId, final String nextQuestionSectionId, final UserStats userStats) {
        @Nullable final Quiz quiz = getQuiz();

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable String correctAnswer = null;
        if (!result) {
            correctAnswer = quiz.getAnswer(questionId);
        }

        @Nullable final Question nextQuestion = getNextQuestionFromUserStatsForSection(nextQuestionSectionId, quiz, userStats);
        return new SubmissionResult(result, correctAnswer, nextQuestion);
    }

    private void storeAnswer(boolean result, @NotNull final Question question, final String userId, @Nullable final Map<String, UserStats> mapUserStats) {
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
            userStats = new UserStats(userId, sectionId);
        }

        storeAnswerForSection(result, question, userId, userStats);
    }

    private void storeAnswerForSection(boolean result, @Nullable final Question question, final String userId, @Nullable UserStats userStats) {
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

            userStats = new UserStats(userId, sectionId);
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
        @Nullable final UserProfile userProfile = getUserProfileImpl();
        if (userProfile == null) {
            //This is normal if the user is not logged in.
            //Log.error("getUserId(): userProfile was null.");
            return null;
        }

        final String userId = userProfile.getId();
        if (StringUtils.isEmpty(userId)) {
            Log.error("getUserId(): userId was null.");
        }

        return userId;
    }

    @NotNull
    private SubmissionResult storeAnswerCorrectnessAndGetSubmissionResult(final String questionId, final String nextQuestionSectionId, @NotNull final QuestionAndAnswer questionAndAnswer, boolean result) {
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
                userStats = getUserStatsForSection(userId, nextQuestionSectionId);
                storeAnswerForSection(result, questionAndAnswer.getQuestion(), userId, userStats);
            }

            return createSubmissionResultForSection(result, questionId, nextQuestionSectionId, userStats);
        } else {
            @Nullable Map<String, UserStats> mapUserStats = null;
            if (!StringUtils.isEmpty(userId)) {
                mapUserStats = getUserStats(userId);
                storeAnswer(result, questionAndAnswer.getQuestion(), userId, mapUserStats);
            }

            return createSubmissionResult(result, questionId, nextQuestionSectionId, mapUserStats);
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
}

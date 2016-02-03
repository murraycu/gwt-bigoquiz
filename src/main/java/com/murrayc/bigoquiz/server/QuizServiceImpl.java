package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.Log;
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
    public Quiz quiz;

    public static Quiz loadQuiz() {
        try(final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml")) {
            if (is == null) {
                final String errorMessage = "quiz.xml not found.";
                Log.fatal(errorMessage);
                return null;
            }

            return QuizLoader.loadQuiz(is);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    public Question getQuestion(final String questionId) throws IllegalArgumentException {
        final Quiz quiz = getQuiz();
        return quiz.getQuestion(questionId);
    }

    @Override
    public Question getNextQuestion(final String sectionId) throws IllegalArgumentException {
        final Quiz quiz = getQuiz();
        return quiz.getRandomQuestion(sectionId);
    }

    @Override
    public QuizSections getSections() throws IllegalArgumentException {
        final Quiz quiz = getQuiz();
        return quiz.getSections();
    }

    @Override
    public SubmissionResult submitAnswer(final String questionId, final String answer, String nextQuestionSectionId) throws IllegalArgumentException {
        final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        //Store whether we got the question right or wrong:
        final boolean result = StringUtils.equals(questionAndAnswer.getAnswer(), answer);
        storeAnswer(result, questionAndAnswer.getQuestion());

        return createSubmissionResult(result, questionId, nextQuestionSectionId);
    }

    @Override
    public SubmissionResult submitDontKnowAnswer(final String questionId, final String nextQuestionSectionId) throws IllegalArgumentException {
        return createSubmissionResult(false, questionId, nextQuestionSectionId);
    }

    private QuestionAndAnswer getQuestionAndAnswer(final String questionId) {
        final Quiz quiz = getQuiz();
        return quiz.getQuestionAndAnswer(questionId);
    }

    @Override
    public UserProfile getUserProfile() throws IllegalArgumentException {
        return getUserProfileImpl();
    }

    @Override
    public UserRecentHistory getUserRecentHistory() throws IllegalArgumentException {
        final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            Log.error("getUserRecentHistory(): userId was null.");
            return null;
        }

        final Quiz quiz = getQuiz();
        final QuizSections sections = quiz.getSections();
        if (sections == null) {
            return null;
        }

        final EntityManagerFactory emf = EntityManagerFactory.get();

        //Get the stats for this user, for each section:
        final UserRecentHistory result = new UserRecentHistory(userId, sections);
        for (final String sectionId : sections.getSectionIds()) {
            UserStats userStats = getUserStats(userId, sectionId);
            if (userStats == null) {
                //So we get the default values:
                userStats = new UserStats(userId, sectionId);
            }

            //Set the titles.
            //We don't store these in the datastore because we can get them easily from the Quiz.
            //TODO: It might really be more efficient to store them in the datastore.
            for (final UserQuestionHistory userQuestionHistory : userStats.getQuestionHistories()) {
                final Question question = quiz.getQuestion(userQuestionHistory.getQuestionId());
                if (question != null) {
                    userQuestionHistory.setQuestionTitle(question.getText());
                }

                final String subSectionTitle = sections.getSubSectionTitle(question.getSectionId(),
                        question.getSubSectionId());
                userQuestionHistory.setSubSectionTitle(subSectionTitle);
            }

            result.setSectionStats(sectionId, userStats);
        }

        return result;
    }

    @Override
    public void resetSections() {
        final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            Log.error("resetSections(): userId was null.");
            return;
        }

        //TODO: Get the keys only:
        final EntityManagerFactory emf = EntityManagerFactory.get();
        Query<UserStats> q = emf.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        final List<UserStats> list = q.list();
        if (list.isEmpty()) {
            //Presumably, they don't exist yet, or have already been deleted.
            return;
        }

        for (final UserStats userStats : list) {
            //TODO: Batch these:
            emf.ofy().delete().entity(userStats).now();
        }
    }

    private UserStats getUserStats(final String userId, final String sectionId) {
        final EntityManagerFactory emf = EntityManagerFactory.get();
        Query<UserStats> q = emf.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        q = q.filter("sectionId", sectionId);
        q = q.limit(1);
        final List<UserStats> list = q.list();
        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
    }

    private UserProfile getUserProfileImpl() {
        final User user = getUser();
        if (user == null) {
            return null;
        }

        final EntityManagerFactory emf = EntityManagerFactory.get();
        UserProfile userProfile = emf.ofy().load().type(UserProfile.class).id(user.getUserId()).now();
        if (userProfile == null) {
            userProfile = new UserProfile(user.getUserId(), user.getNickname());
            emf.ofy().save().entity(userProfile).now();
        }
        return userProfile;
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
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }

        context.setAttribute(LOADED_QUIZ, quiz);

        return quiz;
    }

    private SubmissionResult createSubmissionResult(boolean result, final String questionId, final String nextQuestionSectionId) {
        //We only provide the correct answer if the supplied answer was wrong:
        String correctAnswer = null;
        if (!result) {
            final Quiz quiz = getQuiz();
            correctAnswer = quiz.getAnswer(questionId);
        }

        final Question nextQuestion = getNextQuestion(nextQuestionSectionId);
        return new SubmissionResult(result, correctAnswer, nextQuestion);
    }

    private void storeAnswer(boolean result, final Question question) {
        final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            Log.error("storeAnswer(): userId was null.");
            return;
        }

        final EntityManagerFactory emf = EntityManagerFactory.get();

        //Update the statistics:
        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("storeAnswer(): sectionId is null.");
            return;
        }

        UserStats userStats = getUserStats(userId, sectionId);
        if (userStats == null) {
            userStats = new UserStats(userId, sectionId);
        }

        userStats.incrementAnswered();

        if (result) {
            userStats.incrementCorrect();
        }

        userStats.updateProblemQuestion(question, result);

        emf.ofy().save().entity(userStats).now();
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

    private String getUserId() {
        final UserProfile userProfile = getUserProfile();
        if (userProfile == null) {
            Log.error("getUserId(): userProfile was null.");
            return null;
        }

        final String userId = userProfile.getId();
        if (StringUtils.isEmpty(userId)) {
            Log.error("getUserId(): userId was null.");
        }

        return userId;
    }
}

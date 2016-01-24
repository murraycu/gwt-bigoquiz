package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserAnswer;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QuizServiceImpl extends ServiceWithUser implements
        QuizService {
    private static final String LOADED_QUIZ = "loaded-quiz";

    //How many items to return.
    public static final int HISTORY_LIMIT = 5;

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

    //@Override
    public UserRecentHistory getUserRecentHistory() throws IllegalArgumentException {
        final User user = getUser();
        if (user == null) {
            return null;
        }

        final EntityManagerFactory emf = EntityManagerFactory.get();
        Query<UserAnswer> q = emf.ofy().load().type(UserAnswer.class);
        q = q.filter("userId", user.getUserId());
        q = q.limit(HISTORY_LIMIT);

        //Objectify's Query.list() method seems to return a list implementation that contains
        //some kind of (non-serializable) proxy, leading to gwt compilation errors such as this:
        //  com.google.gwt.user.client.rpc.SerializationException: Type 'com.sun.proxy.$Proxy10' was not included in the set of types which can be serialized by this SerializationPolicy or its Class object could not be loaded. For security purposes, this type will not be serialized.: instance = [com.murrayc.bigoquiz.shared.db.UserAnswer@7a44340b]
        //so we copy the items into a new list.
        //Presumably the act of iterating over the list causes us to actually get the data for each item,
        //as the actual type.
        //
        //This also gives us the opportunity to fill in the question title,
        //which we want to give to the client, but which we didn't want to store
        //along with each UserAnswer.
        final List<UserAnswer> listCopy = new ArrayList<>();
        for (final UserAnswer a : q.list()) {
            if (a == null) {
                continue;
            }

            a.setQuestionTitle(getQuestionTitle(a.getQuestionId()));

            listCopy.add(a);
        }
        return new UserRecentHistory(listCopy);
    }

    private String getQuestionTitle(final String questionId) {
        final Quiz quiz = getQuiz();
        final Question question = quiz.getQuestion(questionId);
        if (question == null) {
            return null;
        }

        return question.getText();
    }

    private String getSectionTitle(final String sectionId) {
        final Quiz quiz = getQuiz();
        return quiz.getSectionTitle(sectionId);
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
        final UserProfile userProfile = getUserProfileImpl();
        if (userProfile == null) {
            //TODO: Keep a score in the session, without a user profile?
            return;
        }

        final EntityManagerFactory emf = EntityManagerFactory.get();

        if (result) {
            userProfile.setCountCorrectAnswers(userProfile.getCountCorrectAnswers() + 1);

            emf.ofy().save().entity(userProfile).now();
        }

        final String time = getCurrentTime();
        final UserAnswer userAnswer = new UserAnswer(userProfile.getId(), question, result, time);
        emf.ofy().save().entity(userAnswer).now();
    }

    private static String getCurrentTime() {
        //TODO: Performance:
        final TimeZone tz = TimeZone.getTimeZone("UTC");
        final DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mmZ");
        df.setTimeZone(tz);
        return df.format(new Date());
    }
}

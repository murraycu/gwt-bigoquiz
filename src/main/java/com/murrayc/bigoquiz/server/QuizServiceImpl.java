package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.server.db.UserAnswer;
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
import java.util.Date;
import java.util.TimeZone;

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
    public Question getNextQuestion() throws IllegalArgumentException {
        final Quiz quiz = getQuiz();
        return quiz.getRandomQuestion();
    }

    public SubmissionResult submitAnswer(final String questionId, final String answer) throws IllegalArgumentException {
        final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        final boolean result = StringUtils.equals(questionAndAnswer.getAnswer(), answer);
        storeAnswer(result, questionId);

        return createSubmissionResult(result, questionId);
    }

    public SubmissionResult submitDontKnowAnswer(final String questionId) throws IllegalArgumentException {
        return createSubmissionResult(false, questionId);
    }

    private QuestionAndAnswer getQuestionAndAnswer(final String questionId) {
        final Quiz quiz = getQuiz();
        return quiz.getQuestionAndAnswer(questionId);
    }

    @Override
    public UserProfile getUserProfile() throws IllegalArgumentException {
        return getUserProfileImpl();
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

    private SubmissionResult createSubmissionResult(boolean result, final String questionId) {
        //We only provide the correct answer if the supplied answer was wrong:
        String correctAnswer = null;
        if (!result) {
            final Quiz quiz = getQuiz();
            correctAnswer = quiz.getAnswer(questionId);
        }

        final Question nextQuestion = getNextQuestion();
        return new SubmissionResult(result, correctAnswer, nextQuestion);
    }

    private void storeAnswer(boolean result, final String questionId) {
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
        final UserAnswer userAnswer = new UserAnswer(userProfile.getId(), questionId, result, time);
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

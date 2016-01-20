package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import com.murrayc.bigoquiz.shared.Question;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.InputStream;

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

    public boolean submitAnswer(final String questionId, final String answer) throws IllegalArgumentException {
        final Question question = getQuestion(questionId);
        if (question == null) {
            throw new IllegalArgumentException("Unknown Question ID");
        }

        final boolean result = StringUtils.equals(question.getAnswer(), answer);
        if (result) {
            increaseScore();
        }

        return result;
    }


    @Override
    public UserProfile getUserProfile() throws IllegalArgumentException {
        return getUserProfileImpl();
    }

    @Override
    public void increaseScore() throws IllegalArgumentException {
        final UserProfile userProfile = getUserProfileImpl();
        if (userProfile == null) {
            //TODO: Keep a score in the session, without a user profile?
            return;
        }

        userProfile.setCountCorrectAnswers(userProfile.getCountCorrectAnswers() + 1);

        final EntityManagerFactory emf = EntityManagerFactory.get();
        emf.ofy().put(userProfile);
    }

    private UserProfile getUserProfileImpl() {
        final User user = getUser();
        if (user == null) {
            return null;
        }

        final EntityManagerFactory emf = EntityManagerFactory.get();
        UserProfile userProfile = emf.ofy().find(UserProfile.class, user.getUserId());
        if (userProfile == null) {
            userProfile = new UserProfile(user.getUserId(), user.getNickname());
            emf.ofy().put(userProfile);
        }
        return userProfile;
    }

    private Quiz getQuiz() {
        final ServletConfig config = this.getServletConfig();
        if(config == null) {
            Log.error("getServletConfig() return null");
            return null;
        }

        final ServletContext context = config.getServletContext();
        if(context == null) {
            Log.error("getServletContext() return null");
            return null;
        }

        //Use the existing shared quiz if any:
        final Object object = context.getAttribute(LOADED_QUIZ);
        if((object != null) && !(object instanceof Quiz)) {
            Log.error("The loaded-quiz attribute is not of the expected type.");
            return null;
        }

        Quiz quiz = (Quiz)object;
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

    public static Quiz loadQuiz() {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml");
        if (is == null) {
            final String errorMessage = "quiz.xml not found.";
            Log.fatal(errorMessage);
            return null;
        }

        return QuizLoader.loadQuiz(is);
    }

    public Quiz quiz;
}

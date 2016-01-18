package com.murrayc.bigoquiz.server;

import com.allen_sauer.gwt.log.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.murrayc.bigoquiz.shared.Question;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.io.InputStream;

/**
 * The server-side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class QuizServiceImpl extends RemoteServiceServlet implements
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
    String userAgent = getThreadLocalRequest().getHeader("User-Agent");

    // Escape data from the client to avoid cross-site script vulnerabilities.
    input = escapeHtml(input);
    userAgent = escapeHtml(userAgent);

    return "Hello, " + input + "!<br><br>I am running " + serverInfo
        + ".<br><br>It looks like you are using:<br>" + userAgent;
  }
  */

    @Override
    public Question getQuestion() throws IllegalArgumentException {
        final Quiz quiz = getQuiz();
        return quiz.getRandomQuestion();
    }

    /**
     * Escape an html string. Escaping data received from the client helps to
     * prevent cross-site script vulnerabilities.
     *
     * @param html the html string to escape
     * @return the escaped string
     */
    private String escapeHtml(String html) {
        if (html == null) {
            return null;
        }
        return html.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(
                ">", "&gt;");
    }

    public Quiz getQuiz() {
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

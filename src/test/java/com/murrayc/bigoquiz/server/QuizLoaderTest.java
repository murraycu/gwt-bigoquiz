package com.murrayc.bigoquiz.server;

import static org.junit.Assert.*;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoaderTest extends TestCase  {

    @Test
    public void testLoadQuiz() throws Exception {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml");
        if (is == null) {
            final String errorMessage = "quiz.xml not found.";
            Log.fatal(errorMessage);
            return null;
        }

        final Quiz quiz = QuizLoader.loadQuiz(is);
        assertNotNull(quiz);
    }
}
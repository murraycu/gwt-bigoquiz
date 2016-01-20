package com.murrayc.bigoquiz.server;

import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertNotNull;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoaderTest {

    @Test
    public void testLoadQuiz() throws Exception {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml");
        assertNotNull(is);

        final Quiz quiz = QuizLoader.loadQuiz(is);
        assertNotNull(quiz);
    }
}
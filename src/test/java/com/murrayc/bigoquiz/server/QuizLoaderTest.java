package com.murrayc.bigoquiz.server;

import com.allen_sauer.gwt.log.client.Log;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoaderTest extends TestCase {

    @Test
    public void testLoadQuiz() throws Exception {
        final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml");
        assertNotNull(is);

        final Quiz quiz = QuizLoader.loadQuiz(is);
        assertNotNull(quiz);
    }
}
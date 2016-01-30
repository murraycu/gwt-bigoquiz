package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.QuizSections;
import org.junit.Test;

import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoaderTest {

    @Test
    public void testSections() throws Exception {
        final Quiz quiz = loadQuiz();
        assertNotNull(quiz);

        final QuizSections sections = quiz.getSections();
        assertNotNull(sections);

        assertEquals(sections.getSectionTitle("heap-operations"), "Heap Operations");

        final QuizSections.SubSection subSection = sections.getSubSection("heap-operations", "fibonacci-heap");
        assertNotNull(subSection);
        assertEquals(subSection.title, "Fibonacci Heap");
    }

    private static Quiz loadQuiz() throws Exception {
        try (final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("quiz.xml")) {
            assertNotNull(is);

            final Quiz quiz = QuizLoader.loadQuiz(is);
            assertNotNull(quiz);
            return quiz;
        }
    }
}
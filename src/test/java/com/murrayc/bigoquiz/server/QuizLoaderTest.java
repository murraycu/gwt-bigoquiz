package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.InputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuizLoaderTest {
    @Nullable
    private static Quiz loadQuiz() throws Exception {
        try (final InputStream is = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("bigo.xml")) {
            assertNotNull(is);

            @Nullable final Quiz quiz = QuizLoader.loadQuiz(is);
            assertNotNull(quiz);
            return quiz;
        }
    }

    @Test
    public void testSections() throws Exception {
        @Nullable final Quiz quiz = loadQuiz();
        assertNotNull(quiz);

        @NotNull final QuizSections sections = quiz.getSections();
        assertNotNull(sections);
        assertEquals(6, sections.getSectionIds().size());

        assertEquals(sections.getSectionTitle("heap-operations"), "Heap Operations");

        @Nullable final QuizSections.SubSection subSection = sections.getSubSection("heap-operations", "fibonacci-heap");
        assertNotNull(subSection);
        assertEquals(subSection.getTitle(), "Fibonacci Heap");
    }

    @Test
    public void testGetQuestionsForSection() throws Exception {
        @Nullable final Quiz quiz = loadQuiz();
        assertNotNull(quiz);

        final List<QuestionAndAnswer> questions = quiz.getQuestionsForSection("array-sorting-algorithms");
        assertNotNull(questions);
        assertEquals(39, questions.size());
    }
}
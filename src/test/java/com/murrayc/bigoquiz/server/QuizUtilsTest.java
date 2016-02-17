package com.murrayc.bigoquiz.server;

import org.junit.Test;

/**
 * Created by murrayc on 2/17/16.
 */
public class QuizUtilsTest {
    @Test
    public void testAnswerIsCorrect() {
        testBothWays("O(log(n))", " O(log(n))");
    }

    @Test
    public void testAnswerIsCorrectLeadingSpaces() {
        testBothWays("O(log(n))", "  O(log(n))");
    }

    @Test
    public void testAnswerIsCorrectTrailingSpaces() {
        testBothWays("O(log(n))", "O(log(n))  ");
    }

    @Test
    public void testAnswerIsCorrectLeadingAndTrailingSpaces() {
        testBothWays("O(log(n))", "   O(log(n))  ");
    }

    @Test
    public void testAnswerIsCorrectSpacesInMiddle() {
        testBothWays("O(log(n))", "O( log(n))");
    }

    @Test
    public void testAnswerIsCorrectCaseInsenstive() {
        testBothWays("O(log(n))", "O( LOG(n))");
    }

    public void testBothWays(final String a, final String b) {
        assert(QuizUtils.answerIsCorrect(a, b));
        assert(QuizUtils.answerIsCorrect(b, a));
    }
}

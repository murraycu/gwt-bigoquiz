package com.murrayc.bigoquiz.server;

/**
 * Created by murrayc on 2/17/16.
 */
public class QuizUtils {
    static boolean answerIsCorrect(final String answer, final String correctAnswer) {
        boolean result = false;
        if (answer == null) {
            result = false;
        } else {
            result = answer.replaceAll("\\s+", "").equalsIgnoreCase(correctAnswer.replaceAll("\\s+",""));
        }
        return result;
    }
}

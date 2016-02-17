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
            result = canonicalize(answer).equalsIgnoreCase(canonicalize(correctAnswer));
        }
        return result;
    }

    static String canonicalize(final String str) {
        if (str == null) {
            return "";
        }

        String result = str.replaceAll("\\s+", "");
        result = result.replaceAll("²", "^2");
        result = result.replaceAll("³", "^3");
        result = result.replaceAll("⁴", "^4");
        return result;
    }
}

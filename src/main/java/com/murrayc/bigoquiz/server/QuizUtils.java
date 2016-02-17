package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.StringUtils;

/**
 * Created by murrayc on 2/17/16.
 */
public class QuizUtils {
    static boolean answerIsCorrect(final String answer, final String correctAnswer, boolean exact) {
        boolean result = false;
        if (answer == null) {
            return false;
        } else if (exact) {
            return StringUtils.equals(correctAnswer, answer);
        } else {
            return canonicalize(answer).equalsIgnoreCase(canonicalize(correctAnswer));
        }
    }

    static String canonicalize(final String str) {
        if (str == null) {
            return "";
        }

        //TODO: Performance
        String result = str.replaceAll("\\s+", "");
        result = result.replaceAll("²", "^2");
        result = result.replaceAll("³", "^3");
        result = result.replaceAll("⁴", "^4");
        return result;
    }
}

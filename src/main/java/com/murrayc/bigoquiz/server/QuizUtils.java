package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.StringUtils;

/**
 * Created by murrayc on 2/17/16.
 */
public class QuizUtils {

    static boolean answerIsCorrect(final String answer, final String correctAnswer) {
        if (answer == null) {
            return false;
        }

        return StringUtils.equals(correctAnswer, answer);
    }
}

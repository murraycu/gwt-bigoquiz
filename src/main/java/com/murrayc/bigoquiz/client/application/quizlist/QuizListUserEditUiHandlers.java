package com.murrayc.bigoquiz.client.application.quizlist;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created by murrayc on 1/21/16.
 */
interface QuizListUserEditUiHandlers extends UiHandlers {
    void onAnswerQuestions(final String quizId);
    void onHistory(final String id);
}

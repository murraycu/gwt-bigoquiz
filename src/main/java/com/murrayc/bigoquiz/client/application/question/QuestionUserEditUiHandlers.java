package com.murrayc.bigoquiz.client.application.question;

import com.gwtplatform.mvp.client.UiHandlers;

/**
 * Created by murrayc on 1/21/16.
 */
interface QuestionUserEditUiHandlers extends UiHandlers {
    void onSubmitAnswer();

    void onShowAnswer();

    void onGoToNextQuestion();

    void onNextQuestionSectionSelected(final String nextQuestionSectionId);
}

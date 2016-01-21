package com.murrayc.bigoquiz.client.ui;

import com.murrayc.bigoquiz.shared.QuestionAndAnswer;

/**
 * Created by murrayc on 1/19/16.
 */
public interface QuestionView extends View {
    void setQuestion(final QuestionAndAnswer questionAndAnswer);
    String getChoiceSelected();
    void setSubmissionResult(boolean submissionResult);

    interface Presenter extends View.Presenter {
        void submitAnswer();
    }
}

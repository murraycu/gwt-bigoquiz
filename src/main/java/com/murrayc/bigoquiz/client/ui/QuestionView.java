package com.murrayc.bigoquiz.client.ui;

import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/19/16.
 */
public interface QuestionView extends View {
    void setQuestion(final Question question);
}

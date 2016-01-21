package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.user.client.ui.Label;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionView extends ViewImpl implements QuestionPresenter.MyView {
    QuestionView() {
        initWidget(new Label("Hello World!"));
    }
}

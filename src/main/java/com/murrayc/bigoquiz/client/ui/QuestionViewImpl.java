package com.murrayc.bigoquiz.client.ui;

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/19/16.
 */
public class QuestionViewImpl extends Composite implements QuestionView {
    private Presenter presenter;

    private Label questionLabel = new Label("question text");
    private Label answerLabel = new Label("answer text");

    public QuestionViewImpl() {
        final FlowPanel box = new FlowPanel();
        //box.getElement().setAttribute("id", "titlebox");

        box.add(new Label("Question"));
        box.add(questionLabel);
        box.add(answerLabel);

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.add(box);
        initWidget(mainPanel);
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {

    }

    @Override
    public void setQuestion(final Question question) {
        questionLabel.setText(question == null ? "" : question.getQuestion());
        answerLabel.setText(question == null ? "" : question.getAnswer());
    }
}

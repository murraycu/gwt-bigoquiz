package com.murrayc.bigoquiz.client.ui;

import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/19/16.
 */
public class QuestionViewImpl extends Composite implements QuestionView {
    private Presenter presenter;

    private Label questionLabel = new Label("question text");
    private Label answerLabel = new Label("answer text");
    private Panel choicesPanel = new VerticalPanel();

    public QuestionViewImpl() {
        final FlowPanel box = new FlowPanel();
        //box.getElement().setAttribute("id", "titlebox");

        box.add(new Label("Question"));
        box.add(questionLabel);
        box.add(answerLabel);
        box.add(choicesPanel);

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
        choicesPanel.clear();

        if (question == null) {
            questionLabel.setText("");
            answerLabel.setText("");
            return;
        }

        questionLabel.setText(question.getQuestion());
        answerLabel.setText(question.getAnswer());

        for (final String choice : question.getChoices()) {
            final CheckBox checkBox = new CheckBox(choice);
            choicesPanel.add(checkBox);
        }
    }
}

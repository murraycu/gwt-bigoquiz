package com.murrayc.bigoquiz.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/19/16.
 */
public class QuestionViewImpl extends Composite implements QuestionView {
    private Presenter presenter;

    private Label questionLabel = new Label("question text");
    private Panel choicesPanel = new VerticalPanel();
    private String choiceSelected;
    private Label resultLabel = new Label("result");


    public QuestionViewImpl() {
        final FlowPanel box = new FlowPanel();
        //box.getElement().setAttribute("id", "titlebox");

        box.add(new Label("QuestionAndAnswer"));
        box.add(questionLabel);
        box.add(choicesPanel);
        box.add(resultLabel);

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.add(box);
        initWidget(mainPanel);
    }

    @Override
    public void setPresenter(final View.Presenter presenter) {
        this.presenter = (QuestionView.Presenter)presenter;
    }

    @Override
    public void clear() {

    }

    @Override
    public void setQuestion(final Question question) {
        choicesPanel.clear();

        if (question == null) {
            questionLabel.setText("");
            return;
        }

        questionLabel.setText(question.getText());

        final String groupName = "choices";
        for (final String choice : question.getChoices()) {
            final RadioButton radioButton = new RadioButton(groupName, choice);
            radioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(final ValueChangeEvent<Boolean> event) {
                    if (event.getValue()) {
                        submitAnswer(choice);
                    }
                }
            });

            choicesPanel.add(radioButton);
        }

        resultLabel.setText("waiting");
    }

    @Override
    public String getChoiceSelected() {
        return choiceSelected;
    }

    @Override
    public void setSubmissionResult(boolean submissionResult) {
        if(submissionResult) {
            resultLabel.setText("Correct");
        } else {
            resultLabel.setText("Wrong");
        }
    }

    private void submitAnswer(final String answer) {
        choiceSelected = answer;
        presenter.submitAnswer();
    }
}

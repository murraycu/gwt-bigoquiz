package com.murrayc.bigoquiz.client.ui;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;

/**
 * Created by murrayc on 1/19/16.
 */
public class QuestionViewImpl extends Composite implements QuestionView {
    private Presenter presenter;

    private Label questionLabel = new Label("question text");
    private Label answerLabel = new Label("answer text");
    private Panel choicesPanel = new VerticalPanel();
    private String choiceSelected;
    private Label resultLabel = new Label("result");


    public QuestionViewImpl() {
        final FlowPanel box = new FlowPanel();
        //box.getElement().setAttribute("id", "titlebox");

        box.add(new Label("QuestionAndAnswer"));
        box.add(questionLabel);
        box.add(answerLabel);
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
    public void setQuestion(final QuestionAndAnswer questionAndAnswer) {
        choicesPanel.clear();

        if (questionAndAnswer == null) {
            questionLabel.setText("");
            answerLabel.setText("");
            return;
        }

        questionLabel.setText(questionAndAnswer.getQuestion());
        answerLabel.setText(questionAndAnswer.getAnswer());

        final String groupName = "choices";
        for (final String choice : questionAndAnswer.getChoices()) {
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

package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.StringUtils;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionView extends ViewWithUiHandlers<QuestionUserEditUiHandlers>
        implements QuestionPresenter.MyView {
    private Label questionLabel = new Label("");
    private Panel choicesPanel = new VerticalPanel();
    private String choiceSelected;

    private FlowPanel resultPanel = new FlowPanel();
    private Button showAnswerButton = new Button("Show Answer");
    private Button nextQuestionButton = new Button("Next");
    private Label correctAnswerLabel = new Label();
    private Label resultLabel = new Label();

    QuestionView() {
        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");
        //mainPanel.getElement().setAttribute("id", "titlebox");

        final Label titleLabel = new Label("Question");
        titleLabel.addStyleName("page-title-label");
        mainPanel.add(titleLabel);

        mainPanel.add(questionLabel);
        questionLabel.addStyleName("question-label");
        mainPanel.add(choicesPanel);
        choicesPanel.addStyleName("choices-panel");

        resultPanel.addStyleName("result-panel");
        resultPanel.add(resultLabel);
        resultLabel.addStyleName("result-label");

        resultPanel.add(showAnswerButton);
        showAnswerButton.addStyleName("show-answer-button");

        showAnswerButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                onShowAnswerButton();
            }
        });
        resultPanel.add(correctAnswerLabel);

        resultPanel.add(nextQuestionButton);
        nextQuestionButton.addStyleName("next-question-button");

        nextQuestionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                onNextQuestionButton();
            }
        });
        mainPanel.add(resultPanel);

        initWidget(mainPanel);
    }

    private void onShowAnswerButton() {
        getUiHandlers().onShowAnswer();
    }

    private void onNextQuestionButton() {
        getUiHandlers().onGoToNextQuestion();
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
            radioButton.addStyleName("question-radio-button");
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

        updateResultPanelUi(State.WAITING_FOR_ANSWER);
        resultLabel.setText("");
    }

    @Override
    public String getChoiceSelected() {
        return choiceSelected;
    }

    @Override
    public void setSubmissionResult(final QuizService.SubmissionResult submissionResult) {
        if (submissionResult == null) {
            Log.error("setSubmissionResult(): submissionResult was null.");
            return;
        }

        //This is empty if the answer was correct:
        //correctAnswerLabel.setText(submissionResult.getCorrectAnswer());
        updateResultPanelUi(submissionResult.getResult() ? State.CORRECT_ANSWER : State.WRONG_ANSWER);
    }

    @Override
    public void showAnswer(final String correctAnswer) {
        if (!StringUtils.isEmpty(correctAnswer)) {
            //We have the correct answer from the result of a previously-wrong subsmission:
            correctAnswerLabel.setText(correctAnswer);
        }

        updateResultPanelUi(State.DONT_KNOW_ANSWER);
    }

    private void submitAnswer(final String answer) {
        choiceSelected = answer;
        getUiHandlers().onSubmitAnswer();
    }

    private void updateResultPanelUi(final State state) {
        switch (state) {
            case WAITING_FOR_ANSWER: {
                showAnswerButton.setVisible(true);
                nextQuestionButton.setVisible(false);
                correctAnswerLabel.setVisible(false);
                resultLabel.setVisible(false);
                break;
            }
            case DONT_KNOW_ANSWER: {
                showAnswerButton.setVisible(false); //No need to click it again.
                nextQuestionButton.setVisible(true);
                correctAnswerLabel.setVisible(true);

                resultLabel.setText("Don't Know");
                resultLabel.setVisible(true);
                break;
            }
            case WRONG_ANSWER: {
                showAnswerButton.setVisible(true);
                nextQuestionButton.setVisible(false);
                correctAnswerLabel.setVisible(false);
                resultLabel.setText("Wrong");
                resultLabel.setVisible(true);
                break;
            }
            case CORRECT_ANSWER: {
                showAnswerButton.setVisible(false);
                nextQuestionButton.setVisible(true);
                correctAnswerLabel.setVisible(false);
                resultLabel.setText("Correct");
                resultLabel.setVisible(true);
            }
        }
    }

    private enum State {
        WAITING_FOR_ANSWER,
        DONT_KNOW_ANSWER,
        WRONG_ANSWER,
        CORRECT_ANSWER
    }
}

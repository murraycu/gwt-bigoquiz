package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;

import java.util.Iterator;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionView extends ViewWithUiHandlers<QuestionUserEditUiHandlers>
        implements QuestionPresenter.MyView {
    //Map of section IDs to section titles.
    private QuizSections sections;
    private String nextQuestionSectionId;
    private String choiceSelected;

    private Label labelSectionTitle = new Label();
    private ListBox nextQuestionSectionListBox = new ListBox();
    private Label questionLabel = new Label("");
    private Panel choicesPanel = new VerticalPanel();

    private FlowPanel resultPanel = new FlowPanel();
    private Button showAnswerButton = new Button("Show Answer");
    private Button nextQuestionButton = new Button("Next");
    private Label resultLabel = new Label();

    QuestionView() {
        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        final Label sectiontitle = new Label("Section:");
        sectiontitle.addStyleName("page-title-label");
        mainPanel.add(sectiontitle);

        mainPanel.add(labelSectionTitle);
        labelSectionTitle.addStyleName("section-title");

        final Label nextQuestionSectiontitle = new Label("Showing Questions from:");
        nextQuestionSectiontitle.addStyleName("page-title-label");
        mainPanel.add(nextQuestionSectiontitle);
        mainPanel.add(nextQuestionSectionListBox);
        nextQuestionSectionListBox.addStyleName("next-question-section-title");
        nextQuestionSectionListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
                //QuestionView.this.nextQuestionSectionId = nextQuestionSectionListBox.getSelectedValue();
                final String nextQuestionSectionId = getSelectedNextQuestionSectionId();
                getUiHandlers().onNextQuestionSectionSelected(nextQuestionSectionId);
            }
        });

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

    private String getSelectedNextQuestionSectionId() {
        final String title = nextQuestionSectionListBox.getSelectedValue();
        return sections.getIdForTitle(title);
    }

    private void onShowAnswerButton() {
        getUiHandlers().onShowAnswer();
    }

    private void onNextQuestionButton() {
        getUiHandlers().onGoToNextQuestion();
    }

    @Override
    public void setSections(final QuizSections sections) {
        this.sections = sections;

        if (sections == null) {
            nextQuestionSectionListBox.clear();
            return;
        }

        nextQuestionSectionListBox.clear();

        //TODO: Give this a special DI/boolean-marker when we can use a proper assocative ListBox:
        nextQuestionSectionListBox.addItem("All Sections");

        for(final String title : sections.getTitles()) {
            if (!StringUtils.isEmpty(title)) {
                nextQuestionSectionListBox.addItem(title);
            }
        }

        setNextQuestionSectionTitle(nextQuestionSectionId);
    }

    @Override
    public void setNextQuestionSectionId(final String sectionId) {
        nextQuestionSectionId = sectionId;

        String title = null;
        if (sections != null) {
            title = sections.getSectionTitle(nextQuestionSectionId);
        }
        setNextQuestionSectionTitle(title);
    }

    private void setNextQuestionSectionTitle(final String sectionTitle) {
        //TODO: Use a derived/better ListBox that lets us refere to the items by ID.
        final int count = nextQuestionSectionListBox.getItemCount();
        for (int i = 0; i < count; ++i) {
            if (StringUtils.equals(
                    nextQuestionSectionListBox.getItemText(i), sectionTitle)) {
                nextQuestionSectionListBox.setSelectedIndex(i);
                break;
            }
        }
    }

    @Override
    public void setQuestion(final Question question) {
        Window.setTitle("Big-O Algorithms Quiz" + ": " + "Question" + ": " + question.getText());

        choicesPanel.clear();

        if (question == null) {
            questionLabel.setText("");
            return;
        }

        questionLabel.setText(question.getText());

        labelSectionTitle.setText(sections.getSectionTitle(question.getSectionId()));

        final String groupName = "choices";
        for (final String choice : question.getChoices()) {
            final RadioButton radioButton = new RadioButton(groupName, choice);
            radioButton.addStyleName("question-radio-button");
            radioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                @Override
                public void onValueChange(final ValueChangeEvent<Boolean> event) {
                    if (event != null && event.getValue()) {
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

        updateResultPanelUi(submissionResult.getResult() ? State.CORRECT_ANSWER : State.WRONG_ANSWER);
    }

    @Override
    public void showAnswer(final String correctAnswer) {
        showCorrectAnswerInChoices(correctAnswer);

        updateResultPanelUi(State.DONT_KNOW_ANSWER);
    }

    private void submitAnswer(final String answer) {
        choiceSelected = answer;
        getUiHandlers().onSubmitAnswer();
    }

    private void updateResultPanelUi(final State state) {
        enableChoices(true);

        switch (state) {
            case WAITING_FOR_ANSWER: {
                showAnswerButton.setVisible(true);
                nextQuestionButton.setVisible(false);
                resultLabel.setVisible(false);
                break;
            }
            case DONT_KNOW_ANSWER: {
                //Don't let them choose the correct answer right after we've shown them the correct answer:
                enableChoices(false);

                showAnswerButton.setVisible(false); //No need to click it again.
                nextQuestionButton.setVisible(true);

                //resultLabel.setText("Don't Know");
                //resultLabel.setVisible(true);
                resultLabel.setVisible(false); //Showing "Don't Know" is annoying to the user.
                break;
            }
            case WRONG_ANSWER: {
                showAnswerButton.setVisible(true);
                nextQuestionButton.setVisible(false);
                resultLabel.setText("Wrong");
                resultLabel.setVisible(true);
                break;
            }
            case CORRECT_ANSWER: {
                //Don't let them immediately submit another correct answer,
                //because they now know the correct answer.
                enableChoices(false);

                showAnswerButton.setVisible(false);
                nextQuestionButton.setVisible(true);
                resultLabel.setText("Correct");
                resultLabel.setVisible(true);
            }
        }
    }

    /** Disable all radio buttons in the the choicesPanel.
     * We need this helper method because there is no general Panel.setEnabled() method.
     *
     * @param enabled
     */
    private void enableChoices(boolean enabled) {
        final Iterator<Widget> iter = choicesPanel.iterator();
        while (iter.hasNext()) {
            final Widget widget = iter.next();

            if (widget instanceof RadioButton) {
                final RadioButton radioButton = (RadioButton) widget;
                radioButton.setEnabled(enabled);
            }
        }
    }

    private void showCorrectAnswerInChoices(final String correctAnswer) {
        final Iterator<Widget> iter = choicesPanel.iterator();
        while (iter.hasNext()) {
            final Widget widget = iter.next();

            if (widget instanceof RadioButton) {
                final RadioButton radioButton = (RadioButton) widget;
                if (StringUtils.equals(radioButton.getText(), correctAnswer)) {
                    radioButton.addStyleName("question-radio-button-correct");
                    return;
                }
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

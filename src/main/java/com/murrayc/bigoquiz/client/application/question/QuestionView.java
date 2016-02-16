package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionView extends ViewWithUiHandlers<QuestionUserEditUiHandlers>
        implements QuestionPresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    //Map of section IDs to section titles.
    @Nullable
    private QuizSections sections;
    private String nextQuestionSectionId;
    private String choiceSelected;

    private final Label labelError = Utils.createServerErrorLabel(constants);
    private final ListBox nextQuestionSectionListBox = new ListBox();
    private final Label sectionTitle = new InlineLabel();
    private final Anchor subSectionTitle = new Anchor();
    private final Label questionLabel = new InlineLabel();
    private final Panel choicesPanel = new FlowPanel();

    private final Button showAnswerButton = new Button(constants.showAnswerButton());
    private final Button nextQuestionButton = new Button(constants.nextButton());
    private final Label resultLabel = new Label();
    @NotNull
    private State state = State.WAITING_INVALID;

    QuestionView() {
        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        mainPanel.add(labelError);

        @NotNull final Panel showingFromPanel = new FlowPanel(ParagraphElement.TAG);
        showingFromPanel.addStyleName("show-from-panel");
        //TODO: Avoid the " " concatenation:
        @NotNull final Label nextQuestionSectionTitle = new InlineLabel(constants.showQuestionsFrom() + " ");
        nextQuestionSectionTitle.addStyleName("next-question-section-title-label");
        showingFromPanel.add(nextQuestionSectionTitle);
        showingFromPanel.add(nextQuestionSectionListBox);
        nextQuestionSectionListBox.addStyleName("next-question-section-title");
        nextQuestionSectionListBox.addChangeHandler(new ChangeHandler() {
            @Override
            public void onChange(final ChangeEvent event) {
                @Nullable final String nextQuestionSectionId = getSelectedNextQuestionSectionId();
                getUiHandlers().onNextQuestionSectionSelected(nextQuestionSectionId);
            }
        });
        mainPanel.add(showingFromPanel);

        Utils.addHeaderToPanel(2, mainPanel, constants.questionLabel());

        @NotNull final Panel paraHeader = Utils.addParagraphWithChild(mainPanel, sectionTitle);
        paraHeader.add(subSectionTitle);
        Utils.addHeaderToPanel(3, mainPanel, paraHeader);

        Utils.addParagraphWithChild(mainPanel, questionLabel);
        questionLabel.addStyleName("question-label");

        mainPanel.add(choicesPanel);
        choicesPanel.addStyleName("choices-panel");
        choicesPanel.addStyleName("clearfix"); //So it is as high as its children.

        @NotNull FlowPanel resultPanel = new FlowPanel();
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

    @Nullable
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
    public void setSections(@Nullable final QuizSections sections) {
        this.sections = sections;

        if (sections == null) {
            nextQuestionSectionListBox.clear();
            return;
        }

        nextQuestionSectionListBox.clear();

        //TODO: Give this a special DI/boolean-marker when we can use a proper associative ListBox:
        nextQuestionSectionListBox.addItem(constants.allSectionsTitle());

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

        @Nullable String title = null;
        if (StringUtils.isEmpty(sectionId)) {
            title = constants.allSectionsTitle();
        } else if (sections != null) {
            title = sections.getSectionTitle(nextQuestionSectionId);
        }

        setNextQuestionSectionTitle(title);
    }

    private void setNextQuestionSectionTitle(final String sectionTitle) {
        //TODO: Use a derived/better ListBox that lets us refer to the items by ID.
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
    public void setQuestion(@Nullable final Question question) {
        choicesPanel.clear();

        if (question == null) {
            Window.setTitle(messages.windowTitle(""));
            questionLabel.setText("");

            return;
        }

        labelError.setVisible(false);

        Window.setTitle(messages.windowTitle(question.getText()));
        questionLabel.setText(question.getText());

        if (sections == null) {
            Log.error("setQuestion(): sections is null.");
            return;
        }

        //TODO: Make the
        //( <b>Section:</b> some section title )
        //properly internationalized, without putting the <b> tags in the translatable string.
        final String sectionId = question.getSectionId();
        final String sectionTitleStr = sections.getSectionTitle(sectionId);
        if (StringUtils.isEmpty(sectionTitleStr)) {
            Log.error("setQuestion(): sectionTitleStr is empty.");
        } else {
            //TODO: Internationalization:
            sectionTitle.setText(sectionTitleStr +": ");
        }

        @Nullable final QuizSections.SubSection subSection = sections.getSubSection(sectionId, question.getSubSectionId());
        if (subSection != null) {
            subSectionTitle.setText(subSection.title);
            subSectionTitle.setHref(subSection.link); //TODO: Sanitize this HTML that comes from our XML file.
        } else {
            subSectionTitle.setText("error: null subsection for: " + question.getSubSectionId());
            subSectionTitle.setHref("");
        }

        if (question.hasChoices()) {
            @NotNull final String GROUP_NAME = "choices";
            for (final String choice : question.getChoices()) {
                @NotNull final RadioButton radioButton = new RadioButton(GROUP_NAME, choice);
                radioButton.addStyleName("question-radio-button");
                radioButton.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
                    @Override
                    public void onValueChange(@Nullable final ValueChangeEvent<Boolean> event) {
                        if (event != null && event.getValue()) {
                            submitAnswer(choice);
                        }
                    }
                });

                choicesPanel.add(radioButton);
            }
        } else {
            Utils.addParagraphWithText(choicesPanel, constants.errorNoChoices(), "error-label-no-choices");
        }

        updateResultPanelUi(State.WAITING_FOR_ANSWER);
        resultLabel.setText("");
    }

    @Override
    public String getChoiceSelected() {
        return choiceSelected;
    }

    @Override
    public void setSubmissionResult(@Nullable final QuizService.SubmissionResult submissionResult) {
        if (submissionResult == null) {
            Log.error("setSubmissionResult(): submissionResult was null.");
            return;
        }

        updateResultPanelUi(submissionResult.getResult() ? State.CORRECT_ANSWER : State.WAITING_AFTER_WRONG_ANSWER);
    }

    @Override
    public void showAnswer(final String correctAnswer) {
        showCorrectAnswerInChoices(correctAnswer);

        updateResultPanelUi(State.DONT_KNOW_ANSWER);
    }

    @Override
    public boolean isWaiting() {
        return state.isWaiting();
    }

    @Override
    public void setServerFailed() {
        labelError.setVisible(true);
    }

    private void submitAnswer(final String answer) {
        choiceSelected = answer;
        getUiHandlers().onSubmitAnswer();
    }

    private void updateResultPanelUi(@NotNull final State state) {
        this.state = state;
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
            case WAITING_AFTER_WRONG_ANSWER: {
                showAnswerButton.setVisible(true);
                nextQuestionButton.setVisible(false);
                resultLabel.setText(constants.wrongLabel());
                resultLabel.setVisible(true);

                showWrongAnswerInChoices(choiceSelected);
                break;
            }
            case CORRECT_ANSWER: {
                //Don't let them immediately submit another correct answer,
                //because they now know the correct answer.
                enableChoices(false);

                showAnswerButton.setVisible(false);
                nextQuestionButton.setVisible(true);
                resultLabel.setText(constants.correctLabel());
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
        for (final Widget widget : choicesPanel) {
            if (widget instanceof RadioButton) {
                @NotNull final RadioButton radioButton = (RadioButton) widget;
                radioButton.setEnabled(enabled);
            }
        }
    }

    private void showCorrectAnswerInChoices(final String correctAnswer) {
        for (final Widget widget : choicesPanel) {
            if (widget instanceof RadioButton) {
                @NotNull final RadioButton radioButton = (RadioButton) widget;
                if (StringUtils.equals(radioButton.getText(), correctAnswer)) {
                    radioButton.addStyleName("question-radio-button-correct");
                    return;
                }
            }
        }
    }

    private void showWrongAnswerInChoices(final String wrongAnswer) {
        for (final Widget widget : choicesPanel) {
            if (widget instanceof RadioButton) {
                @NotNull final RadioButton radioButton = (RadioButton) widget;
                if (StringUtils.equals(radioButton.getText(), wrongAnswer)) {
                    radioButton.addStyleName("question-radio-button-wrong");
                } else {
                    radioButton.removeStyleName("question-radio-button-wrong");
                }
            }
        }
    }

    private enum State {
        WAITING_INVALID,
        WAITING_FOR_ANSWER,
        WAITING_AFTER_WRONG_ANSWER,
        DONT_KNOW_ANSWER,
        CORRECT_ANSWER;

        public boolean isWaiting() {
            return this == WAITING_FOR_ANSWER || this == WAITING_AFTER_WRONG_ANSWER;
        }
    }
}

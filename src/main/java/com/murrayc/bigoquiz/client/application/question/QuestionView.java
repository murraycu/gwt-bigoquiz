package com.murrayc.bigoquiz.client.application.question;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.*;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.QuizService;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.shared.HasIdAndTitle;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionView extends ContentViewWithUIHandlers<QuestionUserEditUiHandlers>
        implements QuestionPresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);
    private final PlaceManager placeManager;

    //Map of section IDs to section titles.
    @Nullable
    private QuizSections sections = null;
    private String nextQuestionSectionId = null;
    private Question.Text choiceSelected = null;
    private boolean questionHasOnlyTwoAnswers = false;

    private final FlowPanel resultPanel;
    private final ListBox nextQuestionSectionListBox = new ListBox();
    private @NotNull final Hyperlink hyperlinkMultipleChoice = new InlineHyperlink();
    private final Label sectionTitleLabel = new InlineLabel();
    private final Anchor sectionTitleAnchor = new Anchor();
    private final Label subSectionTitleLabel = new InlineLabel();
    private final Anchor subSectionTitleAnchor = new Anchor();

    private final Label questionLabel = new InlineLabel();
    private final Anchor questionAnchor = new Anchor();
    private final HTML questionMarkup = new InlineHTML();

    private final Panel choicesPanel = new FlowPanel();

    private final Button showAnswerButton = new Button(constants.showAnswerButton());
    private final Button nextQuestionButton = new Button(constants.nextButton());
    private final Label resultLabel = new Label();
    private final Label noteLabel = new Label();
    private final Anchor videoAnchor = new Anchor(constants.video());
    private boolean hasVideoUrl = false;
    private final Anchor codeAnchor = new Anchor(constants.exampleCode());
    private boolean hasCodeUrl = false;
    @NotNull
    private State state = State.WAITING_INVALID;
    private boolean multipleChoice = false;
    private TextBox textBox = null;
    private Button submitButton = null;

    @Inject
    QuestionView(final PlaceManager placeManager) {
        this.placeManager = placeManager;

        //Sections sidebar:
        //We use a CSS media query to only show this on wider screens:
        @NotNull Panel sidebarPanelSections = new FlowPanel();
        parentPanel.add(sidebarPanelSections);
        sidebarPanelSections.addStyleName("sidebar-panel-sections");

        @NotNull SimplePanel userHistoryRecentPanel = new SimplePanel();
        sidebarPanelSections.add(userHistoryRecentPanel);
        bindSlot(QuestionPresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);


        //Question content:
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

        @NotNull final Panel multipleChoicePanel = new FlowPanel(ParagraphElement.TAG);
        showingFromPanel.addStyleName("show-as-multiple-choice-panel");
        //TODO: Avoid the " " concatenation:
        @NotNull final Label labelTitle = new InlineLabel(constants.offerMultipleChoice() + " ");
        labelTitle.addStyleName("offer-multiple-choice-title-label");
        multipleChoicePanel.add(labelTitle);
        hyperlinkMultipleChoice.addStyleName("offer-multiple-choice-label");
        multipleChoicePanel.add(hyperlinkMultipleChoice);
        mainPanel.add(multipleChoicePanel);

        Utils.addHeaderToPanel(2, mainPanel, constants.questionLabel());

        @NotNull final Panel paraHeader = Utils.addParagraph(mainPanel, null);
        paraHeader.add(sectionTitleLabel);
        paraHeader.add(sectionTitleAnchor);
        paraHeader.add(subSectionTitleLabel);
        paraHeader.add(subSectionTitleAnchor);
        Utils.addHeaderToPanel(3, mainPanel, paraHeader);

        //We only show one of these (label or anchor) at a time:
        final Panel p = Utils.addParagraphWithChild(mainPanel, questionLabel);
        questionLabel.addStyleName("question-label");
        p.add(questionMarkup);
        questionMarkup.addStyleName("question-label");
        p.add(questionAnchor);
        questionAnchor.addStyleName("question-anchor");

        mainPanel.add(choicesPanel);
        choicesPanel.addStyleName("choices-panel");
        choicesPanel.addStyleName("clearfix"); //So it is as high as its children.

        resultPanel = new FlowPanel();
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

        resultPanel.add(noteLabel);
        noteLabel.addStyleName("note-label");

        resultPanel.add(videoAnchor);
        videoAnchor.addStyleName("video-anchor");

        resultPanel.add(codeAnchor);
        codeAnchor.addStyleName("code-anchor");

        nextQuestionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                onNextQuestionButton();
            }
        });
        mainPanel.add(resultPanel);
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
            Log.fatal("setSections(): userhistorysections is null");

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

        setNextQuestionSectionId(nextQuestionSectionId);
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
    public void setQuestion(final String quizId, @Nullable final Question question, boolean multipleChoice) {

        this.multipleChoice = multipleChoice;

        choicesPanel.clear();

        if (question == null) {
            Window.setTitle(messages.windowTitleQuestion("", ""));
            setTitle("");
            questionLabel.setText("");
            questionMarkup.setHTML("");
            questionAnchor.setText("");
            questionAnchor.setHref("");
            sectionTitleLabel.setText("");
            subSectionTitleLabel.setText("");
            sectionTitleAnchor.setText("");
            subSectionTitleAnchor.setText("");
            subSectionTitleAnchor.setHref("");
            resultPanel.setVisible(false); //TODO: Really clear it?
            nextQuestionSectionListBox.clear();

            return;
        }

        resultPanel.setVisible(true);

        setTitle(question.getQuizTitle());

        setErrorLabelVisible(false);

        setQuestionText(question);

        final String onOff = multipleChoice ? constants.offerMultipleChoiceOn() :
                constants.offerMultipleChoiceOff();
        hyperlinkMultipleChoice.setText(onOff);
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(quizId,
                question.getId(), nextQuestionSectionId, !multipleChoice);
        final String historyToken = placeManager.buildHistoryToken(placeRequest);
        hyperlinkMultipleChoice.setTargetHistoryToken(historyToken);

        if (sections == null) {
            Log.error("setQuestion(): userhistorysections is null.");
            return;
        }

        setQuestionSectionTitle(question);

        setQuestionSubSectionTitle(question);

        if (multipleChoice) {
            buildChoices(question);
        } else {
            if (textBox == null) {
                textBox = new TextBox();
                textBox.addStyleName("question-answer-textbox");
                textBox.addKeyDownHandler(new KeyDownHandler() {

                    public void onKeyDown(final KeyDownEvent event) {
                        if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
                            submitAnswer(new Question.Text(textBox.getText(), false));
                        }
                    }
                });
            }

            textBox.setText("");
            textBox.removeStyleName("question-radio-button-wrong");
            choicesPanel.add(textBox);

            if (submitButton == null) {
                submitButton = new Button(constants.submitButton());

                submitButton.addStyleName("question-submit-button");
                submitButton.addClickHandler(new ClickHandler() {
                    @Override
                    public void onClick(final ClickEvent event) {
                        submitAnswer(new Question.Text(textBox.getText(), false));
                    }
                });
            }

            choicesPanel.add(submitButton);
        }

        updateResultPanelUi(State.WAITING_FOR_ANSWER);
        resultLabel.setText("");

        if (question.getQuizUsesMathML()) {
            // Manually ask MathJax to render any MathML,
            // now that we have put some MathML on the page.
            useAndReloadMathJax();
        }
    }

    private void setQuestionSectionTitle(@Nullable final Question question) {
        //TODO: Make the
        //( <b>Section:</b> some section title )
        //properly internationalized, without putting the <b> tags in the translatable string.
        //TODO: Internationalization:
        final String sectionId = question.getSectionId();
        final QuizSections.Section section = sections.getSection(sectionId);
        fillLabelOrAnchor(section, sectionTitleLabel, sectionTitleAnchor, ": ");
    }

    private void setQuestionSubSectionTitle(@Nullable final Question question) {
        final String sectionId = question.getSectionId();
        @Nullable final QuizSections.SubSection subSection = sections.getSubSection(sectionId, question.getSubSectionId());
        fillLabelOrAnchor(subSection, subSectionTitleLabel, subSectionTitleAnchor, null);
    }

    /** Fill the label if there is no link.
     * Otherwise fill the anchor with the title and href.
     *
     * @param section
     * @param label
     * @param anchor
     * @return Whether the Anchor was used.
     */
    private static void fillLabelOrAnchor(final @Nullable HasIdAndTitle section, final Label label, final Anchor anchor, final String suffix) {
        boolean showAnchor = false;

        if (section == null) {
            //Not all questions have to be in a sub-section:
            //subSectionTitleAnchor.setText("error: null subsection for: " + question.getSubSectionId());
            label.setText("");
            anchor.setText("");
            anchor.setHref("");
        } else {
            final String link = section.getLink();
            String title = section.getTitle();
            if (!StringUtils.isEmpty(title) && !StringUtils.isEmpty(suffix)) {
                title += suffix;
            }

            if (StringUtils.isEmpty(link)) {
                label.setText(title);
            } else {
                anchor.setText(title);
                anchor.setHref(link); //TODO: Sanitize this HTML that comes from our XML file.
                showAnchor = true;
            }
        }

        label.setVisible(!showAnchor);
        anchor.setVisible(showAnchor);
    }

    private void setQuestionText(@Nullable final Question question) {
        // Show the question title in the window title,
        // but only if it is not markup:
        // TODO: Show section title and sub-section title too/instead?
        final Question.Text questionText = question.getText();
        final String windowTitle = questionText.isHtml ? question.getQuizTitle() :
                messages.windowTitleQuestion(question.getQuizTitle(), questionText.text);
        Window.setTitle(windowTitle);

        final String link = question.getLink();
        boolean showMarkup = false, showAnchor = false, showLabel = false;
        if (StringUtils.isEmpty(link)) {
            if (questionText.isHtml) {
                //TODO: Use a modified SimpleHtmlSanitizer?
                questionMarkup.setHTML(SafeHtmlUtils.fromTrustedString(questionText.text));
                showMarkup = true;
            } else {
                questionLabel.setText(questionText.text);
                showLabel = true;
            }
        } else {
            if (questionText.isHtml) {
                //TODO: Use a modified SimpleHtmlSanitizer?
                questionAnchor.setHTML(SafeHtmlUtils.fromTrustedString(questionText.text));
            } else {
                questionAnchor.setText(questionText.text);
            }
            questionAnchor.setHref(link);
            showAnchor = true;
        }

        questionMarkup.setVisible(showMarkup);
        questionLabel.setVisible(showLabel);
        questionAnchor.setVisible(showAnchor);

        final String note = question.getNote();
        if (StringUtils.isEmpty(note)) {
            noteLabel.setText("");
        } else {
            noteLabel.setText(note);
        }

        final String videoUrl = question.getVideoUrl();
        if (StringUtils.isEmpty(videoUrl)) {
            videoAnchor.setHref("");
            hasVideoUrl = false;
        } else {
            videoAnchor.setHref(videoUrl);
            hasVideoUrl = true;
        }

        final String codeUrl = question.getCodeUrl();
        if (StringUtils.isEmpty(codeUrl)) {
            codeAnchor.setHref("");
            hasCodeUrl = false;
        } else {
            codeAnchor.setHref(codeUrl);
            hasCodeUrl = true;
        }
    }

    private void buildChoices(@Nullable Question question) {
        if (question.hasChoices()) {
            @NotNull final String GROUP_NAME = "choices";

            final List<Question.Text> questions = question.getChoices();
            if (questions.size() <= 2) {
                questionHasOnlyTwoAnswers = true;
            }

            for (final Question.Text choice : questions) {
                RadioButton radioButton = null;
                if (choice.isHtml) {
                    // TOOD: Use modified SimpleHtmlSanitizer
                    radioButton = new RadioButton(GROUP_NAME, SafeHtmlUtils.fromTrustedString(choice.text));
                } else {
                    radioButton = new RadioButton(GROUP_NAME, choice.text);
                }

                //TODO: Disable the handlers when rebuilding the widgets?
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
    }

    @Override
    public Question.Text getChoiceSelected() {
        return choiceSelected;
    }

    @Override
    public void setSubmissionResult(@Nullable final QuizService.SubmissionResult submissionResult) {
        if (submissionResult == null) {
            Log.error("setSubmissionResult(): submissionResult was null.");
            return;
        }

        final boolean correct = submissionResult.getResult();
        State state = correct ? State.CORRECT_ANSWER : State.WAITING_AFTER_WRONG_ANSWER;
        if (!correct && questionHasOnlyTwoAnswers) {
            state = State.WRONG_ANSWER_AND_CORRECT_IS_IMPLICITLY_SHOWN;
        }

        updateResultPanelUi(state);
    }

    @Override
    public void showAnswer(final Question.Text correctAnswer) {
        if (multipleChoice) {
            showCorrectAnswerInChoices(correctAnswer);
        } else {
            showCorrectAnswerWithoutChoices(correctAnswer);
        }

        updateResultPanelUi(State.DONT_KNOW_ANSWER);
    }

    @Override
    public boolean isWaiting() {
        return state.isWaiting();
    }

    @Override
    public void setServerFailed() {
        setErrorLabelVisible(true);
    }

    private void submitAnswer(final Question.Text answer) {
        choiceSelected = answer;
        getUiHandlers().onSubmitAnswer();
    }

    private void updateResultPanelUi(@NotNull final State state) {
        this.state = state;
        enableChoices(true);

        switch (state) {
            case WAITING_FOR_ANSWER: {
                showAnswerButton.setVisible(true);
                scrollWidgetIntoView(showAnswerButton);

                nextQuestionButton.setVisible(false);
                resultLabel.setVisible(false);
                noteLabel.setVisible(false);
                setAnchorVisibility(videoAnchor, false);
                setAnchorVisibility(codeAnchor, false);
                break;
            }
            case DONT_KNOW_ANSWER: {
                //Don't let them choose the correct answer right after we've shown them the correct answer:
                enableChoices(false);

                showAnswerButton.setVisible(false); //No need to click it again.
                nextQuestionButton.setVisible(true);
                scrollWidgetIntoView(nextQuestionButton);

                //resultLabel.setText("Don't Know");
                //resultLabel.setVisible(true);
                resultLabel.setVisible(false); //Showing "Don't Know" is annoying to the user.
                noteLabel.setVisible(false);
                setAnchorVisibility(videoAnchor, false);
                setAnchorVisibility(codeAnchor, false);
                break;
            }
            case WAITING_AFTER_WRONG_ANSWER: {
                showAnswerButton.setVisible(true);
                scrollWidgetIntoView(showAnswerButton);
                nextQuestionButton.setVisible(false);
                resultLabel.setText(constants.wrongLabel());
                resultLabel.setVisible(true);
                noteLabel.setVisible(false);
                setAnchorVisibility(videoAnchor, false);
                setAnchorVisibility(codeAnchor, false);

                showWrongAnswerInChoices(choiceSelected);
                break;
            }
            case WRONG_ANSWER_AND_CORRECT_IS_IMPLICITLY_SHOWN: {
                enableChoices(false); //Don't let them choose the correct answer now that it is obvious.
                showAnswerButton.setVisible(false); //unnecessary
                //scrollWidgetIntoView(showAnswerButton);
                nextQuestionButton.setVisible(true);
                resultLabel.setText(constants.wrongLabel());
                resultLabel.setVisible(true);
                noteLabel.setVisible(true);
                setAnchorVisibility(videoAnchor, hasVideoUrl);
                setAnchorVisibility(codeAnchor, hasCodeUrl);

                showWrongAnswerInChoices(choiceSelected);
                break;
            }
            case CORRECT_ANSWER: {
                //Don't let them immediately submit another correct answer,
                //because they now know the correct answer.
                enableChoices(false);

                showAnswerButton.setVisible(false);
                nextQuestionButton.setVisible(true);
                scrollWidgetIntoView(nextQuestionButton);
                resultLabel.setText(constants.correctLabel());
                resultLabel.setVisible(true);
                noteLabel.setVisible(true);
                setAnchorVisibility(videoAnchor, hasVideoUrl);
                setAnchorVisibility(codeAnchor, hasCodeUrl);
            }
        }
    }

    /** Set the visibility of the Anchor,
     * always making it invisible if it has no link.
     *
     * @param anchor
     * @param visible
     */
    private void setAnchorVisibility(final Anchor anchor, boolean visible) {
        // TODO: Anchor.getHref() never returns an empty string,
        // so this doesn't actually work:
        if (visible && StringUtils.isEmpty(anchor.getHref())) {
            visible = false;
        }

        anchor.setVisible(visible);
    }

    private void scrollWidgetIntoView(final Widget widget) {
        //Disabled because it scrolls the widget into the middle of the view on Google Chrome.
        /*
        if (widget == null) {
            return;
        }

        //TODO: GWT's Element is deprecated, though getElement() is not.
        if(widget.getElement() != null) {
            widget.getElement().scrollIntoView();
        }
        */
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

        if (textBox != null) {
            textBox.setEnabled(enabled);
        }
    }

    private void showCorrectAnswerInChoices(final Question.Text correctAnswer) {
        if (correctAnswer == null) {
            Log.error("showCorrectAnswerInChoices: correctAnswer is null.");
            return;
        }

        RadioButton correctRadioButton = null;
        for (final Widget widget : choicesPanel) {
            if (widget instanceof RadioButton) {
                @NotNull final RadioButton radioButton = (RadioButton) widget;
                if (!correctAnswer.isHtml) {
                    if (StringUtils.equals(radioButton.getText(), correctAnswer.text)) {
                        correctRadioButton = radioButton;
                        break;
                    }
                } else {
                    if (StringUtils.equals(radioButton.getHTML(), correctAnswer.text)) {
                        correctRadioButton = radioButton;
                        break;
                    }
                }
            }
        }

        if (correctRadioButton != null) {
            correctRadioButton.addStyleName("question-radio-button-correct");
        } else {
            Log.fatal("showCorrectAnswerInChoices(): RadioButton not found.");
        }
    }

    private void showCorrectAnswerWithoutChoices(final Question.Text correctAnswer) {
        if (textBox == null) {
            Log.error("showCorrectAnswerWithoutChoices(): textBox is null.");
            return;
        }

        if (correctAnswer == null) {
            Log.error("showCorrectAnswerInChoices: correctAnswer is null.");
            return;
        }

        textBox.setText(correctAnswer.text);
        textBox.addStyleName("question-radio-button-correct");
    }

    private void showWrongAnswerInChoices(final Question.Text wrongAnswer) {
        for (final Widget widget : choicesPanel) {
            if (widget instanceof RadioButton) {
                @NotNull final RadioButton radioButton = (RadioButton) widget;
                if (StringUtils.equals(radioButton.getText(), wrongAnswer.text)) {
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
        WRONG_ANSWER_AND_CORRECT_IS_IMPLICITLY_SHOWN,
        DONT_KNOW_ANSWER,
        CORRECT_ANSWER;

        public boolean isWaiting() {
            return this == WAITING_FOR_ANSWER || this == WAITING_AFTER_WRONG_ANSWER;
        }
    }
}

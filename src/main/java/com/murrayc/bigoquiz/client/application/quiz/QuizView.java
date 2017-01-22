package com.murrayc.bigoquiz.client.application.quiz;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizView extends ContentViewWithUIHandlers<QuizUserEditUiHandlers>
        implements QuizPresenter.MyView {
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    // We use this instead of trying to store null as an ID in a HashMap.
    public static final String NO_SUBSECTION_ID = "default-subsection";

    private final Panel panelQuiz = new FlowPanel();

    QuizView() {
        setTitle(constants.quizTitle());

        //Sections sidebar:
        //We use a CSS media query to only show this on wider screens:
        @NotNull Panel sidebarPanelSections = new FlowPanel();
        parentPanel.add(sidebarPanelSections);
        sidebarPanelSections.addStyleName("sidebar-panel-sections");

        @NotNull SimplePanel userHistoryRecentPanel = new SimplePanel();
        sidebarPanelSections.add(userHistoryRecentPanel);
        bindSlot(QuizPresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);


        //Quiz content:
        mainPanel.add(panelQuiz);
        panelQuiz.addStyleName("quiz-panel");
    }

    @Override
    public void setQuiz(final Quiz quiz) {
        panelQuiz.clear();

        if (quiz == null) {
            Log.error("setQuiz(): quiz is null.");
            return;
        }

        setErrorLabelVisible(false);

        Window.setTitle(messages.windowTitleQuiz(quiz.getTitle()));
        Utils.addHeaderToPanel(2, panelQuiz, quiz.getTitle());

        final Button buttonPlay = new Button(constants.buttonAnswerQuestions());
        buttonPlay.addStyleName("quiz-list-button-answer-questions");
        buttonPlay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                getUiHandlers().onAnswerQuestions();
            }
        });
        panelQuiz.add(buttonPlay);

        final QuizSections quizSections = quiz.getSections();
        for(final QuizSections.Section section : quizSections.getSectionsSorted()) {
            if (section == null) {
                Log.error("QuizListView: section is null.");
                continue;
            }

            addSection(panelQuiz, constants, quiz, quizSections, section.getId(), section.getTitle());
        }

        //Add questions that are not in a section:
        addSection(panelQuiz, constants, quiz, quizSections, null, null);

        if (quiz.getUsesMathML()) {
            // Manually ask MathJax to render any MathML,
            // now that we have put some MathML on the page.
            useAndReloadMathJax();
        }
    }

    private static void addSection(@NotNull final Panel panelQuiz, final BigOQuizConstants constants, @NotNull final Quiz quiz, QuizSections quizSections, final String sectionId, final String sectionTitle) {
        final Panel panelSection = new FlowPanel();
        panelSection.addStyleName("quiz-section");
        panelQuiz.add(panelSection);

        Utils.addHeaderToPanel(3, panelSection, sectionTitle);

        final List<QuestionAndAnswer> questions = quiz.getQuestionsForSection(sectionId);
        if (questions == null) {
            Log.error("QuizListView: questions is null.");
            return;
        }

        addQuestionsForSection(panelSection, constants, sectionId, quizSections, questions);
    }

    /** Add the subsections (and their questions), including questions which are not in a sub-section.
     *  @param panelSection
     * @param constants
     * @param sectionId
     * @param quizSections
     * @param questions
     */
    private static void addQuestionsForSection(final Panel panelSection, final BigOQuizConstants constants, final String sectionId, final QuizSections quizSections, final List<QuestionAndAnswer> questions) {
        final Map<String, List<QuestionAndAnswer>> questionsBySubSection = groupQuestionsBySubSection(questions);
        if (questionsBySubSection == null) {
            Log.error("QuizView: addQuestionsForSection(): questionsBySubSection is null.");
            return;
        }

        if (StringUtils.isEmpty(sectionId)) {
            Log.error("QuizView: addQuestionsForSection(): sectionId is null.");
            return;
        }

        if (quizSections == null) {
            Log.error("QuizView: addQuestionsForSection(): quizSections is null.");
            return;
        }

        final List<QuizSections.SubSection> sorted = quizSections.getSubSectionsSorted(sectionId);
        if (sorted == null) {
            Log.error("QuizView: addQuestionsForSection(): sorted is null.");
            return;
        }

        for (final QuizSections.SubSection subSection : sorted) {
            if (subSection == null) {
                Log.fatal("QuizListView: subSection is null.");
                continue;
            }

            final String subSectionId = subSection.getId();
            if (subSectionId == null) {
                Log.fatal("QuizListView: subSectionId is null.");
                continue;
            }
            addSubSection(panelSection, constants, questionsBySubSection.get(subSectionId), subSection);
        }

        //Add questions that have no sub-section:
        final List<QuestionAndAnswer> questionsWithoutSubSection = questionsBySubSection.get(NO_SUBSECTION_ID);
        if (questionsWithoutSubSection != null && !questionsWithoutSubSection.isEmpty()) {
            addSubSection(panelSection, constants, questionsWithoutSubSection, null);
        }
    }

    /**
     *  @param panelSection
     * @param constants
     * @param questions
     * @param subSection This may be null.
     */
    private static void addSubSection(final Panel panelSection, final BigOQuizConstants constants, final List<QuestionAndAnswer> questions, final QuizSections.SubSection subSection) {
        final Panel panelSubSection = new FlowPanel();
        panelSubSection.addStyleName("quiz-sub-section");
        panelSection.add(panelSubSection);

        if (subSection != null) {
            // We can't use an Anchor with a null href - that creates a link to /null.
            Widget subSectionTitle = null;
            final String link = subSection.getLink();
            if (StringUtils.isEmpty(link)) {
                final InlineLabel label = new InlineLabel();
                label.setText(subSection.getTitle());
                subSectionTitle = label;
            } else {
                final Anchor anchor = new Anchor();
                anchor.setText(subSection.getTitle());
                anchor.setHref(link); //TODO: Sanitize this HTML that comes from our XML file.
                subSectionTitle = anchor;
            }
            Utils.addHeaderToPanel(4, panelSubSection, subSectionTitle);
        }

        if (questions == null) {
            Log.error("addSubSection: questions is null.");
            return;
        }

        for (final QuestionAndAnswer questionAndAnswer : questions) {
            if (questionAndAnswer == null) {
                Log.error("QuizListView: questionAndAnswer is null.");
                continue;
            }

            addQuestionAndAnswer(panelSubSection, constants, questionAndAnswer);
        }
    }

    private static void addQuestionAndAnswer(final Panel panelSubSection, final BigOQuizConstants constants, final QuestionAndAnswer questionAndAnswer) {
        final Question question = questionAndAnswer.getQuestion();
        if (question == null) {
            Log.error("QuizListView: question is null.");
            return;
        }

        final Panel panelQuestionAnswer = new FlowPanel();
        panelQuestionAnswer.addStyleName("quiz-question-answer");
        panelSubSection.add(panelQuestionAnswer);

        final Panel paraQuestion = Utils.addParagraph(panelQuestionAnswer, "");

        final Label labelQuestionTitle = new InlineLabel(constants.question());
        labelQuestionTitle.addStyleName("quiz-question-title");
        paraQuestion.add(labelQuestionTitle);

        final String link = question.getLink();
        final Question.Text questionText = question.getText();
        Widget labelQuestion = null;
        if (StringUtils.isEmpty(link)) {
            if (questionText.isHtml) {
                //TODO: Use a modified SimpleHtmlSanitizer?
                labelQuestion = new InlineHTML(SafeHtmlUtils.fromTrustedString(questionText.text));
            } else {
                labelQuestion = new InlineLabel(questionText.text);
            }
        } else {
            if (questionText.isHtml) {
                //TODO: Use a modified SimpleHtmlSanitizer?
                labelQuestion = new Anchor(SafeHtmlUtils.fromTrustedString(questionText.text), link);
            } else {
                labelQuestion = new Anchor(questionText.text, link);
            }
        }
        labelQuestion.addStyleName("quiz-question");
        paraQuestion.add(labelQuestion);

        final Panel paraAnswer = Utils.addParagraph(panelQuestionAnswer, "");

        final Question.Text answerText = questionAndAnswer.getAnswer();
        if (answerText == null) {
            Log.error("QuizListView: answerText is null.");
            return;
        }

        final Label labelAnswerTitle = new InlineLabel(constants.answer());
        labelAnswerTitle.addStyleName("quiz-answer-title");
        paraAnswer.add(labelAnswerTitle);

        Widget labelAnswer = null;
        if (answerText.isHtml) {
            labelAnswer = new InlineHTML(SafeHtmlUtils.fromTrustedString(answerText.text));
        } else {
            labelAnswer = new InlineLabel(answerText.text);
        }
        labelAnswer.addStyleName("quiz-answer");
        paraAnswer.add(labelAnswer);

        final String note = question.getNote();
        if (note != null) {
            final Panel paraNote = Utils.addParagraph(panelQuestionAnswer, "");
            final Label labelNoteTitle = new InlineLabel(constants.note());
            labelNoteTitle.addStyleName("quiz-note-title");
            paraNote.add(labelNoteTitle);

            final Widget labelNote = new InlineLabel(note);
            labelNote.addStyleName("quiz-note");
            paraNote.add(labelNote);
        }
    }

    private static @NotNull Map<String, List<QuestionAndAnswer>> groupQuestionsBySubSection(final List<QuestionAndAnswer> questions) {
        final Map<String, List<QuestionAndAnswer>> result = new HashMap<>();

        for (final QuestionAndAnswer questionAndAnswer : questions) {
            if (questionAndAnswer == null) {
                continue;
            }

            final Question question = questionAndAnswer.getQuestion();
            if (question == null) {
                continue;
            }

            String subSectionId = question.getSubSectionId();
            if (subSectionId == null) {
                subSectionId = NO_SUBSECTION_ID;
            }

            List<QuestionAndAnswer> list = result.get(subSectionId);
            if (list == null) {
                list = new ArrayList<>();
                result.put(subSectionId, list);
            }

            list.add(questionAndAnswer);
        }

        return result;
    }
}

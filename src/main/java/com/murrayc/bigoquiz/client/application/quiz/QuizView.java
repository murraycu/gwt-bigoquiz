package com.murrayc.bigoquiz.client.application.quiz;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
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
    private final PlaceManager placeManager;

    @Inject
    QuizView(final PlaceManager placeManager) {
        this.placeManager = placeManager;

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
    public void setQuiz(@NotNull final Quiz quiz) {
        setErrorLabelVisible(false);

        panelQuiz.clear();

        if (quiz == null) {
            Log.error("setQuiz(): quiz is null.");
            return;
        }

        Window.setTitle(messages.windowTitleQuiz(quiz.getTitle()));

        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuizQuestion(quiz.getId());
        final String historyToken = placeManager.buildHistoryToken(placeRequest);
        @NotNull final Hyperlink link = new Hyperlink(quiz.getTitle(), historyToken);
        Utils.addHeaderToPanel(2, panelQuiz, link);

        final QuizSections quizSections = quiz.getSections();
        for(final QuizSections.Section section : quizSections.getSectionsSorted()) {
            if (section == null) {
                Log.error("QuizListView: section is null.");
                continue;
            }

            addSection(panelQuiz, constants, quiz, quizSections, section.id, section.title);
        }

        //Add questions that are not in a section:
        addSection(panelQuiz, constants, quiz, quizSections, null, null);
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
            Log.error("QuizListView: questionsBySubSection is null.");
            return;
        }

        if (StringUtils.isEmpty(sectionId)) {
            Log.error("QuizListView: sectionId is null.");
            return;
        }

        for (final QuizSections.SubSection subSection : quizSections.getSubSectionsSorted(sectionId)) {
            if (subSection == null) {
                Log.fatal("QuizListView: subSection is null.");
                continue;
            }

            final String subSectionId = subSection.id;
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
            final Anchor subSectionTitle = new Anchor();
            subSectionTitle.setText(subSection.title);
            subSectionTitle.setHref(subSection.link); //TODO: Sanitize this HTML that comes from our XML file.
            Utils.addHeaderToPanel(4, panelSubSection, subSectionTitle);
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
        Widget labelQuestion = null;
        if (StringUtils.isEmpty(link)) {
            labelQuestion = new InlineLabel(question.getText());
        } else {
            labelQuestion = new Anchor(question.getText(), link);
        }
        labelQuestion.addStyleName("quiz-question");
        paraQuestion.add(labelQuestion);

        final Panel paraAnswer = Utils.addParagraph(panelQuestionAnswer, "");

        final String answer = questionAndAnswer.getAnswer();
        if (answer == null) {
            Log.error("QuizListView: answer is null.");
            return;
        }

        final Label labelAnswerTitle = new InlineLabel(constants.answer());
        labelAnswerTitle.addStyleName("quiz-answer-title");
        paraAnswer.add(labelAnswerTitle);

        final Label labelAnswer = new InlineLabel(answer);
        labelAnswer.addStyleName("quiz-answer");
        paraAnswer.add(labelAnswer);
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

    @Override
    public void setServerFailed() {
        setErrorLabelVisible(true);
    }
}

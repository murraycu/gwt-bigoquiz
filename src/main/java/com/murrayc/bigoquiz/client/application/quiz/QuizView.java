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

    private final Panel panelQuiz = new FlowPanel();
    private final PlaceManager placeManager;

    @Inject
    QuizView(final PlaceManager placeManager) {
        this.placeManager = placeManager;

        setTitle(constants.quizTitle());

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

            final Panel panelSection = new FlowPanel();
            panelSection.addStyleName("quiz-section");
            panelQuiz.add(panelSection);

            Utils.addHeaderToPanel(3, panelSection, section.title);

            final List<QuestionAndAnswer> questions = quiz.getQuestionsForSection(section.id);
            if (questions == null) {
                Log.error("QuizListView: questions is null.");
                continue;
            }

            final Map<String, List<QuestionAndAnswer>> questionsBySubSection = groupQuestionsBySubSection(questions);
            if (questionsBySubSection == null) {
                Log.error("QuizListView: questionsBySubSection is null.");
                continue;
            }

            final String sectionId = section.id;
            if (StringUtils.isEmpty(sectionId)) {
                Log.error("QuizListView: sectionId is null.");
                continue;
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

                final Panel panelSubSection = new FlowPanel();
                panelSubSection.addStyleName("quiz-sub-section");
                panelSection.add(panelSubSection);

                final Anchor subSectionTitle = new Anchor();
                subSectionTitle.setText(subSection.title);
                subSectionTitle.setHref(subSection.link); //TODO: Sanitize this HTML that comes from our XML file.
                Utils.addHeaderToPanel(4, panelSubSection, subSectionTitle);

                for (final QuestionAndAnswer questionAndAnswer : questionsBySubSection.get(subSectionId)) {
                    if (questionAndAnswer == null) {
                        Log.error("QuizListView: questionAndAnswer is null.");
                        continue;
                    }

                    final Question question = questionAndAnswer.getQuestion();
                    if (question == null) {
                        Log.error("QuizListView: question is null.");
                        continue;
                    }

                    final Panel panelQuestionAnswer = new FlowPanel();
                    panelQuestionAnswer.addStyleName("quiz-question-answer");
                    panelSubSection.add(panelQuestionAnswer);
                    final Label labelQuestion = new Label(messages.question(question.getText()));
                    labelQuestion.addStyleName("quiz-question");
                    panelQuestionAnswer.add(labelQuestion);

                    final String answer = questionAndAnswer.getAnswer();
                    if (answer == null) {
                        Log.error("QuizListView: answer is null.");
                        continue;
                    }

                    final Label labelAnswer = new Label(messages.answer(answer));
                    labelAnswer.addStyleName("quiz-answer");
                    panelQuestionAnswer.add(labelAnswer);
                }
            }

        }
    }

    private @NotNull Map<String, List<QuestionAndAnswer>> groupQuestionsBySubSection(final List<QuestionAndAnswer> questions) {
        final Map<String, List<QuestionAndAnswer>> result = new HashMap<>();

        for (final QuestionAndAnswer questionAndAnswer : questions) {
            if (questionAndAnswer == null) {
                continue;
            }

            final Question question = questionAndAnswer.getQuestion();
            if (question == null) {
                continue;
            }

            final String subSectionId = question.getSubSectionId();
            if (subSectionId == null) {
                continue;
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

package com.murrayc.bigoquiz.client.application.quiz;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewImpl;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
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
public class QuizView extends ViewImpl
        implements QuizPresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Label labelError = Utils.createServerErrorLabel(constants);
    private final Panel panelQuiz = new FlowPanel();

    QuizView() {
        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        mainPanel.add(labelError);

        mainPanel.add(panelQuiz);
        panelQuiz.addStyleName("quiz-panel");

        initWidget(mainPanel);
    }

    @Override
    public void setQuiz(@NotNull final Quiz quiz) {
        panelQuiz.clear();

        Utils.addHeaderToPanel(2, panelQuiz, quiz.getTitle());

        final QuizSections quizSections = quiz.getSections();
        for(final QuizSections.Section section : quizSections.getSectionsSorted()) {
            if (section == null) {
                Log.error("QuizView: section is null.");
                continue;
            }

            final Panel panelSection = new FlowPanel();
            panelSection.addStyleName("quiz-section");
            panelQuiz.add(panelSection);

            Utils.addHeaderToPanel(3, panelSection, section.title);

            final List<QuestionAndAnswer> questions = quiz.getQuestionsForSection(section.id);
            if (questions == null) {
                Log.error("QuizView: questions is null.");
                continue;
            }

            final Map<String, List<QuestionAndAnswer>> questionsBySubSection = groupQuestionsBySubSection(questions);
            if (questionsBySubSection == null) {
                Log.error("QuizView: questionsBySubSection is null.");
                continue;
            }

            final String sectionId = section.id;
            if (StringUtils.isEmpty(sectionId)) {
                Log.error("QuizView: sectionId is null.");
                continue;
            }

            for (final QuizSections.SubSection subSection : quizSections.getSubSectionsSorted(sectionId)) {
                if (subSection == null) {
                    Log.fatal("QuizView: subSection is null.");
                    continue;
                }

                final String subSectionId = subSection.id;
                if (subSectionId == null) {
                    Log.fatal("QuizView: subSectionId is null.");
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
                        Log.error("QuizView: questionAndAnswer is null.");
                        continue;
                    }

                    final Question question = questionAndAnswer.getQuestion();
                    if (question == null) {
                        Log.error("QuizView: question is null.");
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
                        Log.error("QuizView: answer is null.");
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
        labelError.setVisible(true);
    }
}

package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import com.murrayc.bigoquiz.shared.QuizSections;

import java.util.*;

/**
 * Created by murrayc on 1/18/16.
 */
public class Quiz {
    //Map of section ID to (map of question IDs to question):
    private Map<String, Map<String, QuestionAndAnswer>> questions = new HashMap<>();

    private final QuizSections quizSections = new QuizSections();

    //An extra list, used only for getting a random question,
    //regardless of what section it is in.
    private List<QuestionAndAnswer> listQuestions = new ArrayList<>();

    public Quiz() {
        questions = new HashMap<>();
    }

    public void addQuestion(final String sectionId, final QuestionAndAnswer questionAndAnswer) {
        Map<String, QuestionAndAnswer> map = questions.get(sectionId);
        if (map == null) {
            map = new HashMap<>();
            questions.put(sectionId, map);
        }

        map.put(questionAndAnswer.getId(), questionAndAnswer);

        //Store it here too:
        listQuestions.add(questionAndAnswer);
    }

    public Question getRandomQuestion(final String sectionId) {
        if (listQuestions.isEmpty()) {
            return null;
        }

        final int index = new Random().nextInt(listQuestions.size());
        final QuestionAndAnswer questionAndAnswer = listQuestions.get(index);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getQuestion();
        }

        return null;
    }

    public boolean contains(final String questionId) {
        return questions.containsKey(questionId);
    }

    public Question getQuestion(final String questionId) {
        final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getQuestion();
        }

        return null;
    }

    public String getAnswer(final String questionId) {
        final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(questionId);
        if (questionAndAnswer != null) {
            return questionAndAnswer.getAnswer();
        }

        return null;
    }

    public void addSection(final String sectionId, final String sectionTitle, final List<String> defaultChoices) {
        quizSections.addSection(sectionId, sectionTitle, defaultChoices);
    }

    QuestionAndAnswer getQuestionAndAnswer(final String questionId) {
        //Look in every section:
        for (Map<String, QuestionAndAnswer> section : questions.values()) {
            final QuestionAndAnswer questionAndAnswer = section.get(questionId);
            if (questionAndAnswer != null) {
                return questionAndAnswer;
            }
        }

        return null;
    }

    public String getSectionTitle(final String sectionId) {
        return quizSections.getSectionTitle(sectionId);
    }

    public QuizSections getSections() {
        return quizSections;
    }
}

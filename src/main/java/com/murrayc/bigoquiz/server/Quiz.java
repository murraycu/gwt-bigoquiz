package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuestionAndAnswer;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.StringUtils;

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
    private final List<QuestionAndAnswer> listQuestions = new ArrayList<>();

    //This is only for getting a random question from a particular section:
    private final Map<String, List<QuestionAndAnswer>> listSectionQuestions = new HashMap<>();


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

        //And here too:
        List<QuestionAndAnswer> sectionList = listSectionQuestions.get(sectionId);
        if (sectionList == null) {
            sectionList = new ArrayList<>();
            listSectionQuestions.put(sectionId, sectionList);
        }
        sectionList.add(questionAndAnswer);
    }

    public Question getRandomQuestion(final String sectionId) {
        if (!StringUtils.isEmpty(sectionId)) {
            final List<QuestionAndAnswer> sectionQuestions = listSectionQuestions.get(sectionId);
            if ((sectionQuestions != null) && !sectionQuestions.isEmpty()) {
                return getRandomQuestionFromList(sectionQuestions);
            }
        }

        return getRandomQuestionFromList(listQuestions);
    }

    private static Question getRandomQuestionFromList(final List<QuestionAndAnswer> listQuestions) {
        if (listQuestions == null) {
            return null;
        }

        final int index = new Random().nextInt(listQuestions.size());
        final QuestionAndAnswer questionAndAnswer = listQuestions.get(index);
        if (questionAndAnswer == null) {
            Log.error("getRandomQuestionFromList(): QuestionAndAnswer was null.");
            return null;
        }

        return questionAndAnswer.getQuestion();
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
        if (quizSections.containsSection(sectionId)) {
            Log.error("Quiz.addSection(): section already exists. Replacing. sectionId: " + sectionId);
        }

        quizSections.addSection(sectionId, sectionTitle, defaultChoices);
    }

    public void addSubSection(final String sectionId, final String subSectionId, final String subSectionTitle, final String subSectionLink) {
        if (!quizSections.containsSection(sectionId)) {
            Log.error("Quiz.addSection(): section does not already exist. Failing.");
            return;
        }

        quizSections.addSubSection(sectionId, subSectionId, subSectionTitle, subSectionLink);
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

    /*
    public String getSectionTitle(final String sectionId) {
        return quizSections.getSectionTitle(sectionId);
    }
    */

    public QuizSections getSections() {
        return quizSections;
    }
}

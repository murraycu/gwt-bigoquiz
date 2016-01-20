package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.Question;

import java.util.*;

/**
 * Created by murrayc on 1/18/16.
 */
public class Quiz {
    //Map of section ID to (map of question IDs to question):
    private Map<String, Map<String, Question>> questions = new HashMap<>();

    //Map of section ID to default choices:
    private Map<String, List<String>> defaultChoices = new HashMap<>();

    //An extra list, used only for getting a random question,
    //regardless of what section it is in.
    private List<Question> listQuestions = new ArrayList<>();

    public Quiz() {
        questions = new HashMap<>();
    }

    public void addQuestion(final String sectionId, final Question question) {
        Map<String, Question> map = questions.get(sectionId);
        if (map == null) {
            map = new HashMap<String, Question>();
            questions.put(sectionId, map);
        }

        map.put(question.getId(), question);

        //Store it here too:
        listQuestions.add(question);
    }

    public Question getRandomQuestion() {
        if (listQuestions.isEmpty()) {
            return null;
        }

        final int index = new Random().nextInt(listQuestions.size());
        return listQuestions.get(index);
    }

    public Question getQuestion(final String questionId) {
        //Look in every section:
        for (Map<String, Question> section : questions.values()) {
            final Question question = section.get(questionId);
            if (question != null) {
                return question;
            }
        }

        return null;
    }

    public void setDefaultChoices(final String sectionId, final List<String> choices) {
        defaultChoices.put(sectionId, choices);
    }
}

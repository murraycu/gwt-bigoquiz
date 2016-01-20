package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.Question;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by murrayc on 1/18/16.
 */
public class Quiz {
    private Map<String, Question> questions;

    public Quiz() {
        questions = new HashMap<>();
    }

    public void addQuestion(final Question question) {
        questions.put(question.getId(), question);
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) {
            return null;
        }

        return questions.get("1");
    }

    public Question getQuestion(final String questionId) {
        return questions.get(questionId);
    }
}

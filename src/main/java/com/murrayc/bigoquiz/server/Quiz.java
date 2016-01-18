package com.murrayc.bigoquiz.server;

import com.murrayc.bigoquiz.shared.Question;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class Quiz {
    public Quiz() {
        questions = new ArrayList<>();
    }

    public void addQuestion(final Question question) {
        questions.add(question);
    }

    public Question getRandomQuestion() {
        if (questions.isEmpty()) {
            return null;
        }

        return questions.get(1);
    }

    private List<Question> questions;
}

package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuestionAndAnswer implements IsSerializable {
    //Don't make these final, because gwt serialization doesn't support that.

    private /* final */ Question question;
    private /* final */ Question.Text answer;

    public QuestionAndAnswer(final String questionId, final String sectionId, final String subSectionId,
                             final Question.Text questionText, final String questionLink, final Question.Text answerText,
                             final List<Question.Text> choices) {
        this.question = new Question(questionId, sectionId, subSectionId, questionText, questionLink, choices);
        this.answer = answerText;
    }

    public QuestionAndAnswer() {
    }

    public String getId() {
        return question.getId();
    }

    public Question getQuestion() {
        return question;
    }

    public Question.Text getAnswer() {
        return answer;
    }
}

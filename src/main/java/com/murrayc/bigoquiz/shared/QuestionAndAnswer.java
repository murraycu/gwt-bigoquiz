package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Created by murrayc on 1/18/16.
 */
public class QuestionAndAnswer implements IsSerializable {
    //Don't make these final, because gwt serialization doesn't support that.

    private /* final */ Question question;
    private /* final */ String answer;


    public QuestionAndAnswer(final String id, final String question, final String answer, final List<String> choices) {
        this.question = new Question(id, question, choices);
        this.answer = answer;
    }

    public QuestionAndAnswer() {
    }

    public String getId() {
        return question.getId();
    }

    public Question getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }
}

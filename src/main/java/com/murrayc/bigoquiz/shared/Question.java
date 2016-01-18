package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by murrayc on 1/18/16.
 */
public class Question implements IsSerializable {
    public Question(final String question, final String answer) {
        this.question = question;
        this.answer = answer;
    }

    public Question() {
        this.question = null;
        this.answer = null;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }

    //Don't make these final, because gwt serialization doesn't support that.
    private /* final */ String question;
    private /* final */ String answer;
}

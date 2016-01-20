package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * Created by murrayc on 1/18/16.
 */
public class Question implements IsSerializable {
    //Don't make these final, because gwt serialization doesn't support that.
    private /* final */ String id;
    private /* final */ String question;
    private /* final */ String answer;

    public Question(final String id, final String question, final String answer) {
        this.id = id;
        this.question = question;
        this.answer = answer;
    }

    public Question() {
        this.id = null;
        this.question = null;
        this.answer = null;
    }

    public String getId() {
        return id;
    }

    public String getQuestion() {
        return question;
    }

    public String getAnswer() {
        return answer;
    }


}

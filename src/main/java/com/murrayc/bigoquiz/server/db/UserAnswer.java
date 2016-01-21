package com.murrayc.bigoquiz.server.db;

import com.google.gwt.user.client.rpc.IsSerializable;

import javax.persistence.Id;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserAnswer implements IsSerializable {
    @Id
    private Long id;

    private String questionId;
    private boolean result;
    private String time;

    UserAnswer() {
    }

    public UserAnswer(final String questionId, final boolean result, final String time) {
        this.questionId = questionId;
        this.result = result;
        this.time = time;
    }

    public String getQuestionId() {
        return questionId;
    }

    public boolean getResult() {
        return result;
    }

    public String getTime() {
        return time;
    }


}

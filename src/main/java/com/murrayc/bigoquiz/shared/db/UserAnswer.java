package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by murrayc on 1/21/16.
 */
@Entity
public class UserAnswer implements IsSerializable {
    @Id
    private Long id;

    //TODO: I would rather use a Ref<UserProfile> here,
    //but that doesn't seem to GWT-compile for the client side.b
    @Index
    private String userId;

    private String questionId;

    //TODO: Internationalization.
    @Ignore
    private String questionTitle;

    private boolean result;
    private String time;

    public UserAnswer() {
    }

    public UserAnswer(final String userId, final String questionId, final boolean result, final String time) {
        this.userId = userId;
        this.questionId = questionId;
        this.result = result;
        this.time = time;
    }

    public String getUserId() {
        return userId;
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

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(final String questionTitle) {
        this.questionTitle = questionTitle;
    }
}
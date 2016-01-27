package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by murrayc on 1/27/16.
 */
@Entity
public class UserStats implements IsSerializable {
    //This is the User ID.
    @Id
    private String id;
    int answered;
    int correct;

    public UserStats() {
    }

    public UserStats(final String userId) {
        this.id = userId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void incrementAnswered() {
        answered += 1;
    }

    public void incrementCorrect() {
        correct += 1;
    }

    public int getAnswered() {
        return answered;
    }

    public void setAnswered(int answered) {
        this.answered = answered;
    }

    public int getCorrect() {
        return correct;
    }

    public void setCorrect(int correct) {
        this.correct = correct;
    }
}

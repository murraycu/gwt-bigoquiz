package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * Created by murrayc on 1/27/16.
 */
@Entity
public class UserStats implements IsSerializable {
    //TODO: Just use userId as the Id, but then get the query to still work.
    @Id
    Long id;

    @Index
    private String userId;

    @Index
    private String sectionId;

    int answered;
    int correct;

    public UserStats() {
    }

    public UserStats(final String userId, final String sectionId) {
        this.userId = userId;
        this.sectionId = sectionId;
    }

    public String getUserId() {
        return userId;
    }

    public void seUserId(final String userId) {
        this.userId = userId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionid(final String sectionId) {
        this.sectionId = sectionId;
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

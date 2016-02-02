package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.murrayc.bigoquiz.shared.Question;

/**
 * Created by murrayc on 1/21/16.
 */
@Entity
public class UserQuestionHistory implements IsSerializable {
    @Id
    private Long id;

    @Index
    private String questionId;

    /* TODO: We might want these to do overall statistics for all users:
    @Index
    private String subSectionId;

    @Index
    private String sectionId;
    */

    //TODO: Internationalization.
    @Ignore
    private String subSectionTitle;

    //TODO: Internationalization.
    @Ignore
    private String questionTitle;

    public int getCountAnsweredWrong() {
        return countAnsweredWrong;
    }

    //Decrements once for each time the user answers it correctly.
    //Increments once for each time the user answers it wrongly.
    @Index
    private int countAnsweredWrong;

    public UserQuestionHistory() {
    }

    public UserQuestionHistory(final Question question) {
        this.questionId = question.getId();
        this.countAnsweredWrong = 0;

        this.subSectionTitle = question.getSubSectionTitle();
        this.questionTitle = question.getText();

        /*
        this.subSectionId = question.getSubSectionId();
        this.sectionId = question.getSectionId();
         */
    }

    public String getQuestionId() {
        return questionId;
    }

    /*
    public String getSectionId() {
        return sectionId;
    }
    */

    /*
    public String getSubSectionId() {
        return subSectionId;
    }
    */

    public String getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(final String questionTitle) {
        this.questionTitle = questionTitle;
    }

    public void setSubSectionTitle(final String subSectionTitle) {
        this.subSectionTitle = subSectionTitle;
    }

    public String getSubSectionTitle() {
        return subSectionTitle;
    }

    public void adjustCount(boolean result) {
        if (result) {
            countAnsweredWrong -= 1;
        } else {
            countAnsweredWrong += 1;
        }
    }
}

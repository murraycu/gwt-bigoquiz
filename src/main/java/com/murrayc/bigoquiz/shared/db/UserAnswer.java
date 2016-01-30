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
public class UserAnswer implements IsSerializable {
    @Id
    private Long id;

    //TODO: I would rather use a Ref<UserProfile> here,
    //but that doesn't seem to GWT-compile for the client side.b
    @Index
    private String userId;

    private String questionId;

    @Index
    private String sectionId;

    @Index
    private String subSectionId;

    //TODO: Internationalization.
    @Ignore
    private String subSectionTitle;

    //TODO: Internationalization.
    @Ignore
    private String questionTitle;

    private boolean result;

    @Index
    private String time;

    public UserAnswer() {
    }

    /** See also setSubSectionTitle().
     *
     * @param userId
     * @param question
     * @param result
     * @param time
     */
    public UserAnswer(final String userId, final Question question, final boolean result, final String time) {
        this.userId = userId;
        this.questionId = question.getId();
        this.sectionId = question.getSectionId();
        this.subSectionId = question.getSubSectionId();
        this.result = result;
        this.time = time;
    }

    public String getUserId() {
        return userId;
    }

    public String getQuestionId() {
        return questionId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getSubSectionId() {
        return subSectionId;
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

    public String getSubSectionTitle() {
        return subSectionTitle;
    }

    /** These are not stored in the datastore by Objectify.
     *
     * @param subSectionTitle
     * @param question
     */
    public void setTitles(final String subSectionTitle, final Question question) {
        this.subSectionTitle = subSectionTitle;
        this.questionTitle = question.getText();

    }
}

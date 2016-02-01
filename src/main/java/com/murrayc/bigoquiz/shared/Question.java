package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class Question implements IsSerializable {
    private /* final */ String id;
    private /* final */ String sectionId;
    private /* final */ String subSectionId;
    private /* final */ String text;
    private /* final */ List<String> choices;

    private String questionTitle;
    private String subSectionTitle;

    public Question() {
    }

    public Question(final String id, final String sectionId, final String subSectionId, final String text, final List<String> choices) {
        this.id = id;
        this.sectionId = sectionId;
        this.subSectionId = subSectionId;
        this.text = text;
        this.choices = choices;
    }

    public String getId() {
        return id;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getSubSectionId() {
        return subSectionId;
    }

    public String getText() {
        return text;
    }

    public List<String> getChoices() {
        return choices;
    }

    /**
     * @param subSectionTitle
     * @param question
     */
    public void setTitles(final String subSectionTitle, final Question question) {
        this.subSectionTitle = subSectionTitle;
        this.questionTitle = question.getText();

    }

    public String getSubSectionTitle() {
        return subSectionTitle;
    }

    public String getQuestionTitle() {
        return questionTitle;
    }
}
package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class Question implements IsSerializable {
    private /* final */ String id;
    private /* final */ String sectionId;
    private /* final */ String subSectionId;
    private /* final */ String text; //The actual question text.
    private /* final */ String link; //An informative URL.
    private /* final */ List<String> choices; //Possible answers, only one of which is correct.

    private /* final */ String quizTitle;
    private /* final */ String subSectionTitle;

    public Question() {
    }

    public Question(final String id, final String sectionId, final String subSectionId, final String text, final String link, final List<String> choices) {
        this.id = id;
        this.sectionId = sectionId;
        this.subSectionId = subSectionId;
        this.text = text;
        this.link = link;
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

    public String getLink() {
        return link;
    }

    public List<String> getChoices() {
        return choices;
    }

    public boolean hasChoices() {
        return (choices != null) && !choices.isEmpty();
    }

    public void setChoices(final List<String> choices) {
        this.choices = choices;
    }

    /** Set titles, just to save users of Question the bother of having to get them from the Quiz class.
     *
     * @param quizTitle The title of the quiz that this Question is from.
     * @param subSectionTitle The title of the quiz's sub-section that this Question is from.
     * @param question
     */
    public void setTitles(final String quizTitle, final String subSectionTitle, @NotNull final Question question) {
        this.quizTitle = quizTitle;
        this.subSectionTitle = subSectionTitle;
        this.text = question.getText(); //TODO: This is not useful.

    }

    public String getQuizTitle() {
        return quizTitle;
    }

    public String getSubSectionTitle() {
        return subSectionTitle;
    }
}
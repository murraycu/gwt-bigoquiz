package com.murrayc.bigoquiz.shared.db;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
@Entity
public class UserQuestionHistory {
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
    private Question.Text questionTitle;

    private boolean answeredCorrectlyOnce;

    //Decrements once for each time the user answers it correctly.
    //Increments once for each time the user answers it wrongly.
    @Index
    private int countAnsweredWrong;


    public UserQuestionHistory() {
    }

    public UserQuestionHistory(@NotNull final Question question) {
        this.questionId = question.getId();
        this.answeredCorrectlyOnce = false;
        this.countAnsweredWrong = 0;

        final QuizSections.SubSection subSection = question.getSubSection();
        this.subSectionTitle = subSection == null ? null : subSection.getTitle();
        this.questionTitle = question.getText();

        /*
        this.subSectionId = question.getSubSectionId();
        this.sectionId = question.getSectionId();
         */
    }

    public String getQuestionId() {
        return questionId;
    }

    /** This is only for the JSON input.
     *
     * @param questionId
     */
    public void setQuestionId(final String questionId) {
        this.questionId = questionId;
    }

    public int getCountAnsweredWrong() {
        return countAnsweredWrong;
    }

    /** This is only for the JSON input.
     *
     * @param countAnsweredWrong
     */
    public void setCountAnsweredWrong(int countAnsweredWrong) {
        this.countAnsweredWrong = countAnsweredWrong;
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

    public Question.Text getQuestionTitle() {
        return questionTitle;
    }

    public void setQuestionTitle(final Question.Text questionTitle) {
        this.questionTitle = questionTitle;
    }

    public void setSubSectionTitle(final String subSectionTitle) {
        this.subSectionTitle = subSectionTitle;
    }

    public String getSubSectionTitle() {
        return subSectionTitle;
    }

    public boolean getAnsweredCorrectlyOnce() {
        return answeredCorrectlyOnce;
    }

    /** This is only for the JSON input.
     *
     * @param answeredCorrectlyOnce
     */
    public void setAnsweredCorrectlyOnce(boolean answeredCorrectlyOnce) {
        this.answeredCorrectlyOnce = answeredCorrectlyOnce;
    }

    public void adjustCount(boolean result) {
        if (result) {
            answeredCorrectlyOnce = true;
        }

        if (result) {
            countAnsweredWrong -= 1;
        } else {
            countAnsweredWrong += 1;
        }
    }
}

package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.dto.UserStatsDTO;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/27/16.
 */
@Entity
public class UserStats {
    //TODO: Just use userId as the Id, but then get the query to still work.
    @Id
    Long id;

    @Index
    String userId;

    UserStatsDTO details = null;

    public UserStats() {
        details = new UserStatsDTO();
    }

    public UserStats(final String userId, final String quizId, final String sectionId) {
        this.userId = userId;
        details = new UserStatsDTO(quizId, sectionId);
    }

    //TODO? public String getUserId() {
    //    return user.getId();
    //}

    public String getSectionId() {
        return details.getSectionId();
    }

    public void setSectionId(final String sectionId) {
        details.setSectionId(sectionId);
    }

    public void incrementAnswered() {
        details.incrementAnswered();
    }

    public void incrementCorrect() {
        details.incrementCorrect();
    }

    public int getAnswered() {
        return details.getAnswered();
    }

    public void setAnswered(int answered) {
        details.setAnswered(answered);
    }

    public int getCorrect() {
        return details.getCorrect();
    }

    public void setCorrect(int correct) {
        details.setCorrect(correct);
    }

    public void updateProblemQuestion(@Nullable final Question question, boolean answerIsCorrect) {
        details.updateProblemQuestion(question, answerIsCorrect);
    }

    @NotNull
    public List<UserQuestionHistory> getQuestionHistories() {
        return details.getQuestionHistories();
    }

    public int getAnsweredOnce() {
        return details.getAnsweredOnce();
    }

    public int getCorrectOnce() {
        return details.getCorrectOnce();
    }

    public boolean getQuestionWasAnswered(final String questionId) {
        return details.getQuestionWasAnswered(questionId);
    }

    public int getQuestionCountAnsweredWrong(final String questionId) {
        return details.getQuestionCountAnsweredWrong(questionId);
    }

    public UserStatsDTO getDetails() {
        return details;
    }
}

package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.StringUtils;

import java.util.*;

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

    Map<String, UserProblemQuestion> problemQuestions;

    public UserStats() {
        problemQuestions = new HashMap<>();
    }

    public UserStats(final String userId, final String sectionId) {
        this.userId = userId;
        this.sectionId = sectionId;
        problemQuestions = new HashMap<>();
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

    public void updateProblemQuestion(final Question question, boolean answerIsCorrect) {
        if (question == null) {
            Log.error("updateProblemQuestion(): question is null.");
            return;
        }

        final String questionId = question.getId();
        if (StringUtils.isEmpty(questionId)) {
            Log.error("updateProblemQuestion(): questionId is empty.");
            return;
        }

        UserProblemQuestion userProblemQuestion = problemQuestions.get(questionId);

        //Add a new problem question, if necessary, if the answer was wrong:
        if (!answerIsCorrect && userProblemQuestion == null) {
            userProblemQuestion = new UserProblemQuestion(question);
            problemQuestions.put(questionId, userProblemQuestion);
        }

        if (userProblemQuestion != null) {
            //Increase the wrong-answer count:
            userProblemQuestion.adjustCount(answerIsCorrect);
        }
    }

    public Collection<UserProblemQuestion> getProblemQuestions() {
        return problemQuestions.values();
    }
}

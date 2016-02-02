package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserStats;

import java.util.*;

/**
 * Created by murrayc on 1/23/16.
 */
public class UserRecentHistory implements IsSerializable {
    /* Do not make thse final, because then GWT cannot serialize them. */
    private /* final */ String userId;
    private /* final */ QuizSections sections;
    private Map<String, UserStats> sectionStats = new HashMap<>();

    UserRecentHistory() {
        userId = null;
        sections = null;
    }

    public UserRecentHistory(final String userId, final QuizSections sections) {
        this.userId = userId;
        this.sections = sections;
    }

    public void setSectionStats(final String sectionId, final UserStats stats) {
        sectionStats.put(sectionId, stats);
    }

    //TODO: Use gwt codesplit because this is only used on the client?
    /**
     * Add @a userAnswer to the beginning of the list for it section, making sure that
     * there are no more than @max items in that sections's list. If necessary,
     * this removes older items.
     */
    public void addUserAnswerAtStart(final Question question, boolean answerIsCorrect) {
        if (question == null) {
            GWT.log("addUserAnswerAtStart(): question was null.");
            return;
        }

        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            GWT.log("addUserAnswerAtStart(): sectionId was empty.");
            return;
        }

        final UserStats userStats = getStatsWithAdd(question.getSectionId());
        userStats.incrementAnswered();
        if (answerIsCorrect) {
            userStats.incrementCorrect();
        }

        final String questionId = question.getId();
        if (StringUtils.isEmpty(questionId)) {
            GWT.log("addUserAnswerAtStart(): questionId was empty.");
            return;
        }

        userStats.updateProblemQuestion(question, answerIsCorrect);
    }

    public UserStats getStats(final String sectionId ) {
        return sectionStats.get(sectionId);
    }


    public QuizSections getSections() {
        return sections;
    }

    private UserStats getStatsWithAdd(final String sectionId ) {
        UserStats stats = getStats(sectionId);
        if (stats == null) {
            if (userId == null) {
                throw new NullPointerException("getStatsWithAdd() needs a userId.");
            }

            stats = new UserStats(userId, sectionId);
            setStats(sectionId, stats);
        }

        return stats;
    }

    private void setStats(final String sectionId, final UserStats stats) {
        sectionStats.put(sectionId, stats);
    }
}

package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/23/16.
 */
public class UserRecentHistory implements IsSerializable {
    /* Do not make these final, because then GWT cannot serialize them. */
    @NotNull
    private /* final */ String userId;
    @NotNull
    private /* final */ QuizSections sections;
    @NotNull
    private Map<String, UserStats> sectionStats = new HashMap<>();

    UserRecentHistory() {
        userId = null;
        sections = null;
    }

    public UserRecentHistory(@NotNull final String userId, @NotNull final QuizSections sections) {
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
    public void addUserAnswerAtStart(@NotNull final Question question, boolean answerIsCorrect) {
        if (question == null) {
            GWT.log("addUserAnswerAtStart(): question was null.");
            return;
        }

        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            GWT.log("addUserAnswerAtStart(): sectionId was empty.");
            return;
        }

        @NotNull final UserStats userStats = getStatsWithAdd(question.getSectionId());
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


    @NotNull
    public QuizSections getSections() {
        return sections;
    }

    @NotNull
    private UserStats getStatsWithAdd(final String sectionId ) {
        @Nullable UserStats stats = getStats(sectionId);
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

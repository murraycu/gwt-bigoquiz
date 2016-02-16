package com.murrayc.bigoquiz.client;

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
    private /* final */ LoginInfo loginInfo;
    @NotNull
    private /* final */ QuizSections sections;
    @NotNull
    private Map<String, UserStats> sectionStats = new HashMap<>();

    UserRecentHistory() {
        loginInfo = null;
        sections = null;
    }

    /**
     * If the @a loginInfo's user Id is null then we create a mostly-empty set of statistics,
     * just showing the question sections for which a logged-in
     * user could have statistics
     *
     * @param loginInfo
     * @param sections
     */
    public UserRecentHistory(@NotNull final LoginInfo loginInfo, @NotNull final QuizSections sections) {
        this.loginInfo = loginInfo;
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
            Log.error("addUserAnswerAtStart(): question was null.");
            return;
        }

        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("addUserAnswerAtStart(): sectionId was empty.");
            return;
        }

        @NotNull final UserStats userStats = getStatsWithAdd(question.getSectionId());
        userStats.incrementAnswered();
        if (answerIsCorrect) {
            userStats.incrementCorrect();
        }

        final String questionId = question.getId();
        if (StringUtils.isEmpty(questionId)) {
            Log.error("addUserAnswerAtStart(): questionId was empty.");
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
            final String userId = getUserId();
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

    public boolean hasUser() {
        return !StringUtils.isEmpty(getUserId());
    }

    private String getUserId() {
        return loginInfo.getUserId();
    }

    @NotNull
    public LoginInfo getLoginInfo() {
        return loginInfo;
    }
}

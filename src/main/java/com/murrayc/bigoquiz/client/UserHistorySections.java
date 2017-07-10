package com.murrayc.bigoquiz.client;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
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
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserHistorySections {
    @NotNull
    private LoginInfo loginInfo;
    @NotNull
    private QuizSections sections;
    private String quizTitle; //For convenience.
    @NotNull
    private Map<String, UserStats> sectionStats = new HashMap<>();

    UserHistorySections() {
        loginInfo = null;
        sections = null;
        quizTitle = null;
    }

    /**
     * If the @a loginInfo's user Id is null then we create a mostly-empty set of statistics,
     * just showing the question userhistorysections for which a logged-in
     * user could have statistics
     *
     * @param loginInfo
     * @param sections
     */
    public UserHistorySections(@NotNull final LoginInfo loginInfo, @NotNull final QuizSections sections, final String quizTitle) {
        this.loginInfo = loginInfo;
        this.sections = sections;
        this.quizTitle = quizTitle;
    }

    public void setSectionStats(final String sectionId, final UserStats stats) {
        sectionStats.put(sectionId, stats);
    }

    //TODO: Use gwt codesplit because this is only used on the client?
    /**
     * Add @a userAnswer to the beginning of the list for it section, making sure that
     * there are no more than @max items in that userhistorysections's list. If necessary,
     * this removes older items.
     */
    public void addUserAnswerAtStart(@NotNull final String quizId, @NotNull final Question question, boolean answerIsCorrect) {
        if (question == null) {
            Log.error("addUserAnswerAtStart(): question was null.");
            return;
        }

        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("addUserAnswerAtStart(): sectionId was empty.");
            return;
        }

        @NotNull final UserStats userStats = getStatsWithAdd(quizId, question.getSectionId());
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

    public UserStats getStats(final String sectionId) {
        return sectionStats.get(sectionId);
    }

    /** This is just for the JSON output.
     */
    public Map<String, UserStats> getStats() {
        return sectionStats;
    }

    /** This is just for the JSON output.
     */
    public void setStats(final Map<String, UserStats> sectionStats) {
        this.sectionStats = sectionStats;
    }

    @NotNull
    public QuizSections getSections() {
        return sections;
    }

    /** This is just for the JSON input.
     */
    public void setSections(final QuizSections sections) {
        this.sections = sections;
    }

    @NotNull
    private UserStats getStatsWithAdd(final String quizId, final String sectionId ) {
        @Nullable UserStats stats = getStats(sectionId);
        if (stats == null) {
            final String userId = getUserId();
            if (userId == null) {
                throw new NullPointerException("getStatsWithAdd() needs a userId.");
            }

            stats = new UserStats(userId, quizId, sectionId);
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

    @JsonIgnore
    private String getUserId() {
        return loginInfo.getUserId();
    }

    @NotNull
    public LoginInfo getLoginInfo() {
        return loginInfo;
    }

    /**
     * This is only used for the JSON input.
     */
    public void setLoginInfo(final LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    public String getQuizTitle() {
        return quizTitle;
    }

    /**
     * This is only used for the JSON input.
     */
    void setQuizTitle(final String quizTitle) {
        this.quizTitle = quizTitle;
    }
}

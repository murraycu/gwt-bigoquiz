package com.murrayc.bigoquiz.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by murrayc on 1/23/16.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserHistoryOverall implements IsSerializable {
    public static class QuizDetails implements IsSerializable {
        public QuizDetails() {
            this.title = null;
            this.questionsCount = 0;
        }

        public QuizDetails(final String title, int questionsCount) {
            this.title = title;
            this.questionsCount = questionsCount;
        }

        public /* final */ String title;
        public /* final */ int questionsCount;
    }

    /* Do not make these final, because then GWT cannot serialize them. */
    @NotNull
    private /* final */ LoginInfo loginInfo;
    @NotNull
    private /* final */ Map<String, QuizDetails> quizzes = new HashMap<>();
    @NotNull
    private Map<String, UserStats> quizStats = new HashMap<>();

    UserHistoryOverall() {
        loginInfo = null;
    }

    /**
     * If the @a loginInfo's user Id is null then we create a mostly-empty set of statistics,
     * just showing the question userhistorysections for which a logged-in
     * user could have statistics
     *
     * @param loginInfo
     */
    public UserHistoryOverall(@NotNull final LoginInfo loginInfo) {
        this.loginInfo = loginInfo;
    }

    public void setQuizStats(final String quizId, final UserStats stats, final String quizTitle, final int quizQuestionsCount) {
        quizStats.put(quizId, stats);

        final QuizDetails details = new QuizDetails(quizTitle, quizQuestionsCount);
        quizzes.put(quizId, details);
    }

    /**
     * This is only used for the JSON output.
     * @return
     */
    public Map<String, UserStats> getStats() {
        return quizStats;
    }

    public UserStats getStats(final String quizId ) {
        return quizStats.get(quizId);
    }


    @NotNull
    @JsonIgnore
    public Map<String, QuizDetails> getQuizzes() {
        return quizzes;
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

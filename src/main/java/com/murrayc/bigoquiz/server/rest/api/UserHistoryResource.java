package com.murrayc.bigoquiz.server.rest.api;

import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.UserHistoryOverall;
import com.murrayc.bigoquiz.server.ServiceUserUtils;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.restygwt.client.RestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by murrayc on 7/7/17.
 */
@Path("user-history")
public class UserHistoryResource extends ResourceWithQuizzes {
    public UserHistoryResource() {
    }

    @GET
    @Produces("application/json")
    public UserHistoryOverall get(@QueryParam("requestUrl") String requestUrl) {
        getOrLoadQuizzes();

        //Get the stats for this user, for each section:
        //We also return the LoginInfo, so we can show a sign in link,
        //and to avoid the need for a separate call to the server.
        @NotNull LoginInfo loginInfo = ServiceUserUtils.getLoginInfo(requestUrl); //TODO: Check for login

        @NotNull final UserHistoryOverall result = new UserHistoryOverall(loginInfo);

        @Nullable final String userId = loginInfo.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return result;
        }

        final Map<String, UserStats> mapUserStats = getUserStats(userId);

        for (final Quiz quiz : quizzes.map.values()) {
            final String quizId = quiz.getId();
            final UserStats userStats = mapUserStats.get(quizId);
            if (userStats == null) {
                continue;
            }

            result.setQuizStats(quizId, userStats, quiz.getTitle(), quiz.getQuestionsCount());
        }

        return result;
    }

    /**
     * Get a map of quiz ID to UserStats for that quiz, for the specified user.
     *
     * @param userId
     * @return
     */
    @NotNull
    private Map<String, UserStats> getUserStats(@NotNull final String userId) {
        final Query<UserStats> q = getQueryForUserStats(userId);

        @NotNull List<UserStats> listUserStats = q.list();
        @NotNull final Map<String, UserStats> map = new HashMap<>();
        for (@NotNull final UserStats userStats : listUserStats) {
            final String quizId = userStats.getQuizId();
            if (!map.containsKey(quizId)) {
                userStats.makeSane();
                map.put(quizId, userStats);
            } else {
                final UserStats existing = map.get(quizId);
                final UserStats combinedStats = existing.createCombinedUserStatsWithoutQuestionHistories(userStats);
                combinedStats.makeSane();
                map.put(quizId, combinedStats);
            }
        }

        return map;
    }


    private static Query<UserStats> getQueryForUserStats(@NotNull final String userId) {
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        return q;
    }

    private static Query<UserStats> getQueryForUserStats(@NotNull final String userId, @NotNull final String quizId) {
        Query<UserStats> q = getQueryForUserStats(userId);
        q = q.filter("quizId", quizId);
        return q;
    }
}

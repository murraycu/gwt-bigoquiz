package com.murrayc.bigoquiz.server.rest.api;

import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.server.ServiceUserUtils;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.restygwt.client.RestService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
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

    @GET
    @Path("/{quizId}")
    @Produces("application/json")
    public UserHistorySections getByQuizId(@PathParam("quizId") String quizId, @QueryParam("requestUrl") final String requestUrl) {
        final Quiz quiz = getQuiz(quizId);
        if (quiz == null) {
            throw new UnknownQuizException();
        }

        @NotNull final QuizSections sections = quiz.getSections();
        if (sections == null) {
            return null;
        }

        //Get the stats for this user, for each section:
        //We also return the LoginInfo, so we can show a sign in link,
        //and to avoid the need for a separate call to the server.
        @NotNull LoginInfo loginInfo = ServiceUserUtils.getLoginInfo(requestUrl);
        @NotNull final UserHistorySections result = new UserHistorySections(loginInfo, sections, quiz.getTitle());

        //This may be null,
        //in which case we will return a mostly-empty set of user statistics,
        //just to show what is possible when the user is logged in:
        @Nullable final String userId = loginInfo.getUserId();

        @Nullable Map<String, UserStats> mapUserStats = null;
        if (!StringUtils.isEmpty(userId)) {
            mapUserStats = getUserStats(userId, quizId);
        }

        for (final String sectionId : sections.getSectionIds()) {
            if (StringUtils.isEmpty(sectionId)) {
                //This seems wise.
                continue;
            }

            @Nullable UserStats userStats = null;
            if (mapUserStats != null) {
                userStats = mapUserStats.get(sectionId);
            }

            if (userStats == null) {
                //So we get the default values:
                userStats = new UserStats(userId, quizId, sectionId);
            }

            //Set the titles.
            //We don't store these in the datastore because we can get them easily from the Quiz.
            //TODO: It might really be more efficient to store them in the datastore.
            List<String> toRemove = null;
            for (@NotNull final UserQuestionHistory userQuestionHistory : userStats.getTopProblemQuestionHistories()) {
                final String questionId = userQuestionHistory.getQuestionId();
                @Nullable final Question question = quiz.getQuestion(questionId);

                //If the question history is invalid, remember that:
                if (question == null ||
                        !StringUtils.equals(question.getSectionId(), sectionId)) {
                    Log.error("question was null or in the wrong section for id:" + questionId);

                    if (toRemove == null) {
                        toRemove = new ArrayList<>();
                    }

                    toRemove.add(questionId);
                    continue;
                }

                userQuestionHistory.setQuestionTitle(question.getText());

                @Nullable final String subSectionTitle = sections.getSubSectionTitle(question.getSectionId(),
                        question.getSubSectionId());
                userQuestionHistory.setSubSectionTitle(subSectionTitle);
            }

            //Remove any invalid question histories:
            if (toRemove != null) {
                for (final String questionId : toRemove) {
                    userStats.removeQuestionHistory(questionId);
                }

                //Save it so we don't have to remove it next time:
                //TODO: This doesn't seem to work - we have to remove it again next time.
                EntityManagerFactory.ofy().save().entity(userStats).now();
            }

            result.setSectionStats(sectionId, userStats);
        }

        return result;
    }

    /**
     * Get a map of section ID to UserStats for that section, for the specified user.
     *
     * @param userId
     * @return
     */
    @NotNull
    private Map<String, UserStats> getUserStats(@NotNull final String userId, @NotNull final String quizId) {
        final Query<UserStats> q = getQueryForUserStats(userId, quizId);

        @NotNull final Map<String, UserStats> map = new HashMap<>();
        for (@NotNull final UserStats userStats : q.list()) {
            map.put(userStats.getSectionId(), userStats);
        }

        return map;
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

package com.murrayc.bigoquiz.server.rest.api;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.server.QuizzesMap;
import com.murrayc.bigoquiz.server.ServiceUserUtils;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.ws.rs.*;
import java.util.*;

/**
 * Created by murrayc on 7/7/17.
 */
@Path("question")
public class QuestionResource extends ResourceWithQuizzes {

    public QuestionResource() {
    }

    @GET
    @Path("/next")
    @Produces("application/json")
    public Question getNextQuestion(@QueryParam("quiz-id") final String quizId, @QueryParam("section-id") final String sectionId) {
        @NotNull final Quiz quiz = getQuiz(quizId);

        Question result = null;

        @Nullable final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            //The user is not logged in,
            //so just return a random question:
            result = quiz.getRandomQuestion(sectionId);
        } else if (StringUtils.isEmpty(sectionId)) {
            @NotNull final Map<String, UserStats> mapUserStats = getUserStats(userId, quizId);
            result = getNextQuestionFromUserStats(null, quiz, mapUserStats);
        } else {
            //This special case is a bit copy-and-pasty of the general case with the
            //map, but it seems more efficient to avoid an unnecessary Map.
            @Nullable final UserStats userStats = getUserStatsForSection(userId, sectionId, quizId);
            result = getNextQuestionFromUserStatsForSection(sectionId, quiz, userStats);
        }

        if (result != null) {
            setQuestionExtras(result, quiz);
        }

        return result;
    }

    /**
     *
     * @return null if the user is not logged in.
     */
    private String getUserId() {
        @Nullable final User user = ServiceUserUtils.getUser();
        if (user == null) {
            return null;
        }

        return user.getUserId();
    }


    private UserStats getUserStatsForSection(@NotNull final String userId, @NotNull final String quizId, @NotNull final String sectionId) {
        Query<UserStats> q = getQueryForUserStats(userId, quizId);
        q = q.filter("sectionId", sectionId);
        q = q.limit(1);
        final List<UserStats> list = q.list();
        if (!list.isEmpty()) {
            return list.get(0);
        }

        return null;
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


    /**
     *
     * @param sectionId
     * @param quiz
     * @param userStats Can be null.
     * @return
     */
    private Question getNextQuestionFromUserStatsForSection(@NotNull final String sectionId, @NotNull final Quiz quiz, @Nullable final UserStats userStats) {
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("getNextQuestionFromPerSectionUserStat(): sectionId was null.");
            return null;
        }

        //TODO: Avoid this temporary map:
        @NotNull final Map<String, UserStats> map = new HashMap<>();
        if (userStats != null) {
            map.put(userStats.getSectionId(), userStats);
        }

        return getNextQuestionFromUserStats(sectionId, quiz, map);
    }

    @Nullable
    private Question getNextQuestionFromUserStats(@Nullable final String sectionId, @NotNull final Quiz quiz, @Nullable final Map<String, UserStats> mapUserStats) {
        final int MAX_TRIES = 10;
        int tries = 0;
        @Nullable Question question = null;
        @Nullable Question questionBestSoFar = null;
        int questionBestCountAnsweredWrong = 0;
        while(tries < MAX_TRIES) {
            tries += 1;

            question = quiz.getRandomQuestion(sectionId);
            if (question == null) {
                continue;
            }

            if (questionBestSoFar == null) {
                questionBestSoFar = question;
            }

            if (mapUserStats == null) {
                //Assume this means the user has never answered any question in any section.
                return question;
            }

            final UserStats userStats = mapUserStats.get(question.getSectionId());
            if (userStats == null) {
                //Assume this means the user has never answered any question in the section.
                return question;
            }

            final String questionId = question.getId();

            //Prioritize questions that have never been asked.
            if (!userStats.getQuestionWasAnswered(questionId)) {
                return question;
            }

            //Otherwise, try a few times to get a question that
            //we have got wrong many times:
            //We could just get the most-wrong answer directly,
            //but we want some randomness.
            final int countAnsweredWrong = userStats.getQuestionCountAnsweredWrong(questionId);
            if (countAnsweredWrong > questionBestCountAnsweredWrong) {
                questionBestSoFar = question;
                questionBestCountAnsweredWrong = countAnsweredWrong;
            }
        }

        return questionBestSoFar;
    }

    private void setQuestionExtras(final Question question, @NotNull Quiz quiz) {
        String subSectionTitle = null;
        @NotNull final QuizSections sections = quiz.getSections();
        if (sections != null) {
            subSectionTitle = sections.getSubSectionTitle(question.getSectionId(),
                    question.getSubSectionId());
        }
        question.setTitles(quiz.getTitle(), subSectionTitle, question);

        question.setQuizUsesMathML(quiz.getUsesMathML());
    }
}

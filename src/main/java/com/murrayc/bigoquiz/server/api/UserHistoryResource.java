package com.murrayc.bigoquiz.server.api;

import com.google.appengine.api.users.User;
import com.googlecode.objectify.cmd.Query;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.server.QuizUtils;
import com.murrayc.bigoquiz.server.ServiceUserUtils;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.*;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    @GET
    @Produces("application/json")
    public UserHistoryOverall get(@QueryParam("request-url") String requestUrl) {
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
    @Path("/{quiz-id}")
    @Produces("application/json")
    public UserHistorySections getByQuizId(@PathParam("quiz-id") String quizId, @QueryParam("request-url") final String requestUrl) {
        final Quiz quiz = getQuiz(quizId);

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
     * Clear all question answer history, progress, scores, etc.
     */
    @POST
    @Path("/reset-sections")
    public void resetSections(@QueryParam("quiz-id") final String quizId) {
        @Nullable final String userId = getUserId();
        if (StringUtils.isEmpty(userId)) {
            //TODO: Throw some NotLoggedIn exception?
            Log.error("resetSections(): userId is null.");
            return;
        }

        if (StringUtils.isEmpty(quizId)) {
            throw new IllegalArgumentException("Empty or null quiz ID.");
        }

        //TODO: Get the keys only:
        Query<UserStats> q = EntityManagerFactory.ofy().load().type(UserStats.class);
        q = q.filter("userId", userId);
        q = q.filter("quizId", quizId);
        final List<UserStats> list = q.list();
        if (list.isEmpty()) {
            //Presumably, they don't exist yet, or have already been deleted.
            return;
        }

        for (final UserStats userStats : list) {
            //TODO: Batch these:
            EntityManagerFactory.ofy().delete().entity(userStats).now();
        }
    }

    /**
     * submitAnswer() returns the correct correctAnswer (if the supplied correctAnswer was wrong) and the next question.
     * This avoids the client needing to make multiple calls.
     *
     * @param questionId
     * @param answer
     * @param nextQuestionSectionId
     * @return
     * @throws IllegalArgumentException
     */
    @POST
    @Path("/submit-answer")
    @Produces("application/json")
    @NotNull
    public SubmissionResult submitAnswer(@QueryParam("quiz-id") final String quizId, @QueryParam("question-id") final String questionId, @QueryParam("answer") final String answer, @QueryParam("next-question-section-id") final String nextQuestionSectionId) throws IllegalArgumentException {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(quizId, questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        final Question.Text correctAnswer = questionAndAnswer.getAnswer();
        if (correctAnswer == null) {
            throw new RuntimeException("submitAnswer(): correctAnswer was null.");
        }

        final boolean result = QuizUtils.answerIsCorrect(answer, correctAnswer.text);

        return storeAnswerCorrectnessAndGetSubmissionResult(quizId, questionId, nextQuestionSectionId, questionAndAnswer, result);
    }

    @POST
    @Path("/submit-dont-know-answer")
    @Produces("application/json")
    @NotNull
    public SubmissionResult submitDontKnowAnswer(@QueryParam("quiz-id") final String quizId, @QueryParam("question-id") final String questionId, @QueryParam("next-question-section-id") final String nextQuestionSectionId) throws IllegalArgumentException {
        @Nullable final QuestionAndAnswer questionAndAnswer = getQuestionAndAnswer(quizId, questionId);
        if (questionAndAnswer == null) {
            throw new IllegalArgumentException("Unknown QuestionAndAnswer ID");
        }

        //Store this like a don't know answer:
        return storeAnswerCorrectnessAndGetSubmissionResult(quizId, questionId, nextQuestionSectionId, questionAndAnswer, false);
    }


    @NotNull
    private SubmissionResult storeAnswerCorrectnessAndGetSubmissionResult(final String quizId, final String questionId, final String nextQuestionSectionId, @NotNull final QuestionAndAnswer questionAndAnswer, boolean result) {
        //If the user is logged in, store whether we got the question right or wrong:
        @Nullable final String userId = getUserId();

        final String sectionId = questionAndAnswer.getQuestion().getSectionId();

        // Get the UserStats (or a map of them), and use it for both storing the answer and getting the next question,
        // to avoid getting the UserStats twice from the datastore.
        //
        // Call different methods depending on whether nextQuestionSectionId is specified an as is the same as the
        // questino's section ID, to avoid allocating a Map just containing one UserStats.
        if (!StringUtils.isEmpty(nextQuestionSectionId) &&
                StringUtils.equals(nextQuestionSectionId, sectionId)) {
            @Nullable UserStats userStats = null;
            if (!StringUtils.isEmpty(userId)) {
                userStats = getUserStatsForSection(userId, quizId, nextQuestionSectionId);
                storeAnswerForSection(result, quizId, questionAndAnswer.getQuestion(), userId, userStats);
            }

            return createSubmissionResultForSection(result, quizId, questionId, nextQuestionSectionId, userStats);
        } else {
            @Nullable Map<String, UserStats> mapUserStats = null;
            if (!StringUtils.isEmpty(userId)) {
                mapUserStats = getUserStats(userId, quizId);
                storeAnswer(result, quizId, questionAndAnswer.getQuestion(), userId, mapUserStats);
            }

            return createSubmissionResult(result, quizId, questionId, nextQuestionSectionId, mapUserStats);
        }
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


    @NotNull
    private SubmissionResult createSubmissionResult(boolean result, final String quizId, @NotNull final String questionId, @Nullable final String nextQuestionSectionId, @Nullable final Map<String, UserStats> mapUserStats) {
        @NotNull final Quiz quiz = getQuiz(quizId);

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable Question.Text correctAnswer = null;
        if (!result) {
            correctAnswer = quiz.getAnswer(questionId);
        }

        @Nullable final Question nextQuestion = getNextQuestionFromUserStats(nextQuestionSectionId, quiz, mapUserStats);
        return generateSubmissionResult(result, quiz, correctAnswer, nextQuestion);
    }

    @NotNull
    private SubmissionResult generateSubmissionResult(boolean result, Quiz quiz, Question.Text correctAnswer, Question nextQuestion) {
        setQuestionExtras(nextQuestion, quiz);
        return new SubmissionResult(result, correctAnswer, nextQuestion);
    }

    /**
     *
     * @param result
     * @param questionId
     * @param nextQuestionSectionId
     * @param userStats This may be null if nextQuestionSectionId is null.
     * @return
     */
    @NotNull
    private SubmissionResult createSubmissionResultForSection(boolean result, final String quizId, final String questionId, final String nextQuestionSectionId, final UserStats userStats) {
        @NotNull final Quiz quiz = getQuiz(quizId);

        //We only provide the correct answer if the supplied answer was wrong:
        @Nullable Question.Text correctAnswer = null;
        if (!result) {
            correctAnswer = quiz.getAnswer(questionId);
        }

        @Nullable final Question nextQuestion = getNextQuestionFromUserStatsForSection(nextQuestionSectionId, quiz, userStats);
        return generateSubmissionResult(result, quiz, correctAnswer, nextQuestion);
    }

    private void storeAnswer(boolean result, @NotNull final String quizId, @NotNull final Question question, final String userId, @Nullable final Map<String, UserStats> mapUserStats) {
        if (StringUtils.isEmpty(userId)) {
            Log.error("storeAnswer(): userId was null.");
            return;
        }

        if (mapUserStats == null) {
            Log.error("storeAnswer(): mapUserStats was null.");
            return;
        }

        //Update the statistics:
        final String sectionId = question.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            Log.error("storeAnswer(): sectionId is null.");
            return;
        }

        UserStats userStats = mapUserStats.get(sectionId);
        if (userStats == null) {
            userStats = new UserStats(userId, quizId, sectionId);
        }

        storeAnswerForSection(result, quizId, question, userId, userStats);
    }

    private void storeAnswerForSection(boolean result, @NotNull final String quizId, @Nullable final Question question, final String userId, @Nullable UserStats userStats) {
        if (question == null) {
            Log.error("storeAnswerForSection(): question is null.");
            return;
        }

        //Create new UserStats if necessary,
        //for instance if this is the first time storing an answer for this section.
        if (userStats == null) {
            if (StringUtils.isEmpty(userId)) {
                Log.error("storeAnswerForSection(): userId is empty.");
                return;
            }

            final String sectionId = question.getSectionId();
            if (StringUtils.isEmpty(sectionId)) {
                Log.error("storeAnswerForSection(): sectionId is empty.");
                return;
            }

            userStats = new UserStats(userId, quizId, sectionId);
        }

        userStats.incrementAnswered();

        if (result) {
            userStats.incrementCorrect();
        }

        userStats.updateProblemQuestion(question, result);

        EntityManagerFactory.ofy().save().entity(userStats).now();
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

    @Nullable
    private QuestionAndAnswer getQuestionAndAnswer(final String quizId, final String questionId) {
        @NotNull final Quiz quiz = getQuiz(quizId);
        return quiz.getQuestionAndAnswer(questionId);
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

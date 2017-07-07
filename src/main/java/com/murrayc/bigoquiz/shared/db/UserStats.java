package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Ignore;
import com.googlecode.objectify.annotation.Index;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/27/16.
 */
@Entity
public class UserStats implements IsSerializable {
    public static final int MAX_PROBLEM_QUESTIONS = 5;

    //TODO: Just use userId as the Id, but then get the query to still work.
    @Id
    Long id;

    @Index
    private String userId;

    @Index
    private String quizId;

    @Index
    private String sectionId;

    private int answered;
    private int correct;

    private int countQuestionsAnsweredOnce = 0;
    private int countQuestionsCorrectOnce = 0;

    private Map<String, UserQuestionHistory> questionHistories;

    //Only the problem questions, and only the top few of those, in order:
    @Ignore
    private transient List<UserQuestionHistory> topProblemQuestionHistoriesInOrder = null;
    @Ignore
    private transient int problemQuestionHistoriesCount = 0;

    @Ignore
    private transient boolean cacheIsInvalid = true;

    @Ignore
    private transient Comparator<UserQuestionHistory> comparator = null;

    public UserStats() {
        questionHistories = new HashMap<>();
    }

    public UserStats(final String userId, final String quizId, final String sectionId) {
        this.userId = userId;
        this.quizId = quizId;
        this.sectionId = sectionId;
        questionHistories = new HashMap<>();
    }

    public String getUserId() {
        return userId;
    }

    public void seUserId(final String userId) {
        this.userId = userId;
    }

    public String getQuizId() {
        return quizId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(final String sectionId) {
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

    public void updateProblemQuestion(@Nullable final Question question, boolean answerIsCorrect) {
        if (question == null) {
            Log.error("updateProblemQuestion(): question is null.");
            return;
        }

        final String questionId = question.getId();
        if (StringUtils.isEmpty(questionId)) {
            Log.error("updateProblemQuestion(): questionId is empty.");
            return;
        }

        boolean firstTimeAsked = false;
        boolean firstTimeCorrect = false;
        @Nullable UserQuestionHistory userQuestionHistory = questionHistories.get(questionId);

        //Add a new one, if necessary:
        if (userQuestionHistory == null) {
            firstTimeAsked = true;
            if (answerIsCorrect) {
                firstTimeCorrect = true;
            }

            userQuestionHistory = new UserQuestionHistory(question);
            questionHistories.put(questionId, userQuestionHistory);
        } else if (answerIsCorrect && !userQuestionHistory.getAnsweredCorrectlyOnce()) {
            firstTimeCorrect = true;
        }

        //Increase the wrong-answer count:
        userQuestionHistory.adjustCount(answerIsCorrect);

        if (firstTimeAsked) {
            countQuestionsAnsweredOnce++;
        }

        if (firstTimeCorrect) {
            countQuestionsCorrectOnce++;
        }

        cacheIsInvalid = true;
    }

    /** Remove a question history, for instance if the caller has
     * found that the question ID was invalid. This can happen
     * if question IDs change between runs.
     *
     * @param questionId
     */
    public void removeQuestionHistory(final String questionId) {
        @Nullable UserQuestionHistory userQuestionHistory = questionHistories.get(questionId);
        if (userQuestionHistory == null) {
            //It's not there so there's no need to remove it.
            return;
        }

        questionHistories.remove(questionId);
        countQuestionsAnsweredOnce--;

        if (userQuestionHistory.getAnsweredCorrectlyOnce()) {
            countQuestionsAnsweredOnce--;
        }
    }

    private void cacheList() {
        if (!cacheIsInvalid) {
            return;
        }

        if (comparator == null) {
            // Order the problem questions with the most wrong answers first:
            // (reverseOrder doesn't seem to be supported in GWT's client-side.)
            comparator = (o1, o2) -> {
                if (o1 == null) {
                    if (o2 == null) {
                        return 0;
                    }
                }

                if (o2 == null) {
                    return 1;
                }

                final int c1 = o1.getCountAnsweredWrong();
                final int c2 = o2.getCountAnsweredWrong();
                if (c1 == c2) {
                    return 0;
                }

                return (c1 > c2) ?
                        -1 : 1;
            };
        }

        problemQuestionHistoriesCount = 0;
        topProblemQuestionHistoriesInOrder = new ArrayList<>(questionHistories.values());

        topProblemQuestionHistoriesInOrder.sort(comparator);

        //Cache the count of problem questions:
        for (final UserQuestionHistory history : topProblemQuestionHistoriesInOrder) {
            if(history != null && history.getCountAnsweredWrong() > 0) {
                problemQuestionHistoriesCount++;
            }
        }

        //The client only wants the first few:
        final int size = topProblemQuestionHistoriesInOrder.size();
        final int sublistSize = Math.min(size, MAX_PROBLEM_QUESTIONS );
        if (sublistSize != size && sublistSize > 0) {
            topProblemQuestionHistoriesInOrder = topProblemQuestionHistoriesInOrder.subList(0, sublistSize);
        }

        //Don't include questions that are not really problem questions:
        clearNonProblemQuestions();

        cacheIsInvalid = false;
    }

    /**
     * Get the MAX_PROBLEM_QUESTIONS questions that have been answered wrongly the most often.
     * @return
     */
    @NotNull
    public List<UserQuestionHistory> getTopProblemQuestionHistories() {
        cacheList();
        return topProblemQuestionHistoriesInOrder;
    }

    /** Get the count of all the questions that have been answered wrongly.
     * getTopQuestionHistories() returns just the top few of these.
     * @return
     */
    public int getProblemQuestionHistoriesCount() {
        return problemQuestionHistoriesCount;
    }

    public int getAnsweredOnce() {
        return countQuestionsAnsweredOnce;
    }

    public int getCorrectOnce() {
        return countQuestionsCorrectOnce;
    }

    public boolean getQuestionWasAnswered(final String questionId) {
        return questionHistories.containsKey(questionId);
    }

    public int getQuestionCountAnsweredWrong(final String questionId) {
        final UserQuestionHistory history = questionHistories.get(questionId);
        if (history == null) {
            return 0;
        }

        return history.getCountAnsweredWrong();
    }

    //Forget questions that have not been answered wrong more than correct:
    private void clearNonProblemQuestions() {
        //TODO: Instead, use streams with Java 8?
        /*
        topProblemQuestionHistoriesInOrder = topProblemQuestionHistoriesInOrder.stream()
                .filter(p -> p.getCountAnsweredWrong() > 0).collect(Collectors.toList());
        */

        Set<String> idsToRemove = null;
        for (final UserQuestionHistory history : topProblemQuestionHistoriesInOrder) {
            if (history == null) {
                continue;
            }

            if (history.getCountAnsweredWrong() <= 0) {
                if (idsToRemove == null) {
                    idsToRemove = new HashSet<>();
                }

                idsToRemove.add(history.getQuestionId());
            }
        }

        if (idsToRemove == null) {
            return;
        }


        // This is very inefficient but we can't use Java 8 or Apache Commons yet in this class
        // because it is used on the client side too.
        // Luckily, this list only ever small.
        final List<UserQuestionHistory> list = new ArrayList<>();
        for (final UserQuestionHistory item : topProblemQuestionHistoriesInOrder) {
          if (!idsToRemove.contains(item.getQuestionId())) {
              list.add(item);
          }
        }
        topProblemQuestionHistoriesInOrder = list;

        //TODO: Do this instead when we can use Java 8:
        //for (final String id : idsToRemove) {
        //    topProblemQuestionHistoriesInOrder.remove(id);
        //}
    }

    /** Add the values from userStat to this instance,
     * returning a combined UserStats,
     * ignoring the question histories,
     * without changing this instance.
     *
     * @param userStats
     */
    public UserStats createCombinedUserStatsWithoutQuestionHistories(final UserStats userStats) {
        final UserStats result = new UserStats();
        result.userId = this.userId;
        result.quizId = this.quizId;
        result.sectionId = this.sectionId;

        result.answered = this.answered + userStats.answered;
        result.correct = this.correct + userStats.correct;

        result.countQuestionsAnsweredOnce = this.countQuestionsAnsweredOnce + userStats.countQuestionsAnsweredOnce;
        result.countQuestionsCorrectOnce = this.countQuestionsCorrectOnce + userStats.countQuestionsCorrectOnce;

        return result;
    }

    /** Make sure that the values make sense.
     */
    public void makeSane() {
        if (countQuestionsCorrectOnce > countQuestionsAnsweredOnce) {
            countQuestionsCorrectOnce = countQuestionsAnsweredOnce;
        }

        if (correct > answered) {
            correct = answered;
        }
    }
}

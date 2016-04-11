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

    int answered;
    int correct;

    int countQuestionsAnsweredOnce = 0;
    int countQuestionsCorrectOnce = 0;

    Map<String, UserQuestionHistory> questionHistories;

    @Ignore
    private transient List<UserQuestionHistory> questionHistoriesInOrder = null;

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
        //This seems as good a time as any to clear out non-problem questions.
        clearNonProblemQuestions();

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

    private void cacheList() {
        clearNonProblemQuestions();

        if (!cacheIsInvalid) {
            return;
        }

        if (comparator == null) {
            // Order the problem questions with the most wrong answers first:
            // (reverseOrder doesn't seem to be supported in GWT's client-side.)
            comparator = new Comparator<UserQuestionHistory>() {
                @Override
                public int compare(final UserQuestionHistory o1, final UserQuestionHistory o2) {
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
                }
            };
        }

        questionHistoriesInOrder = new ArrayList<>(questionHistories.values());

        //The client only wants the first few:
        final int size = questionHistoriesInOrder.size();
        final int sublistSize = Math.min(size, MAX_PROBLEM_QUESTIONS );
        if (sublistSize != size && sublistSize > 0) {
            questionHistoriesInOrder = questionHistoriesInOrder.subList(0, sublistSize);
        }

        Collections.sort(questionHistoriesInOrder, comparator);
        cacheIsInvalid = false;
    }

    /**
     * Get the MAX_PROBLEM_QUESTIONS questions that have been answered wrongly the most often.
     * @return
     */
    @NotNull
    public List<UserQuestionHistory> getTopQuestionHistories() {
        cacheList();
        return questionHistoriesInOrder;
    }

    /** Get the count of all the questions that have been answered wrongly.
     * getTopQuestionHistories() returns just the top few of these.
     * @return
     */
    public int getQuestionHistoriesCount() {
        if (questionHistories == null) {
            return 0;
        }

        return questionHistories.size();
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
    protected void clearNonProblemQuestions() {
        Set<String> idsToRemove = null;
        for (final UserQuestionHistory history : questionHistories.values()) {
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

        for (final String id : idsToRemove) {
            questionHistories.remove(id);
        }

        cacheIsInvalid = true;
    }
}

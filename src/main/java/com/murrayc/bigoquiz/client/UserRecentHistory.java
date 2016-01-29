package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserProblemQuestion;
import com.murrayc.bigoquiz.shared.db.UserStats;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

import java.util.*;

/**
 * Created by murrayc on 1/23/16.
 */
public class UserRecentHistory implements IsSerializable {
    private QuizSections sections;
    private Map<String, List<UserProblemQuestion>> userProblemQuestions = new HashMap<>();
    private Map<String, UserStats> sectionStats = new HashMap<>();

    UserRecentHistory() {

    }

    public UserRecentHistory(final QuizSections sections) {
        this.sections = sections;
    }

    public void setSectionStats(final String sectionId, final UserStats stats, final List<UserProblemQuestion> problemQuestions) {
        sectionStats.put(sectionId, stats);

        final List<UserProblemQuestion> listProblemQuestions = getProblemQuestionsListWithCreate(sectionId);
        listProblemQuestions.clear();
        listProblemQuestions.addAll(problemQuestions);
    }

    //TODO: Use gwt codesplit because this is only used on the client?
    /**
     * Add @a userAnswer to the beginning of the list for it section, making sure that
     * there are no more than @max items in that sections's list. If necessary,
     * this removes older items.
     *
     * @param userAnswer
     */
    public void addUserAnswerAtStart(final UserAnswer userAnswer) {
        if (userAnswer == null) {
            GWT.log("addUserAnswerAtStart(): userAnswer was null.");
            return;
        }

        final String sectionId = userAnswer.getSectionId();
        if (StringUtils.isEmpty(sectionId)) {
            GWT.log("addUserAnswerAtStart(): sectionId was empty.");
            return;
        }

        final String userId = userAnswer.getUserId();
        /* userId is null on the client,
        because we don't really need it here.
        if (StringUtils.isEmpty(userId)) {
            GWT.log("addUserAnswerAtStart(): userId was empty.");
            return;
        }
        */

        final UserStats userStats = getStatsWithAdd(userId, userAnswer.getSectionId());
        userStats.incrementAnswered();
        if (userAnswer.getResult()) {
            userStats.incrementCorrect();
        }

        final String questionId = userAnswer.getQuestionId();
        if (StringUtils.isEmpty(questionId)) {
            GWT.log("addUserAnswerAtStart(): questionId was empty.");
            return;
        }

        //List a new problem question, if necessary:
        final List<UserProblemQuestion> listProblemQuestions = getProblemQuestionsListWithCreate(sectionId);

        //Add one, if necessary, if the answer was wrong:
        if (!userAnswer.getResult()) {
            UserProblemQuestion toUse = null;
            for (final UserProblemQuestion userProblemQuestion : listProblemQuestions) {
                if (StringUtils.equals(userProblemQuestion.getQuestionId(),
                        questionId)) {
                    toUse = userProblemQuestion;
                    break;
                }
            }

            if (toUse == null) {
                toUse = new UserProblemQuestion(userId, questionId,
                        userAnswer.getSubSectionTitle(), userAnswer.getQuestionTitle(), userAnswer.getSectionId(), userAnswer.getSubSectionId());
                listProblemQuestions.add(0, toUse);
            }
        }

        //It could also be in the list if the current answer is correct:
        for (final UserProblemQuestion userProblemQuestion : listProblemQuestions) {
            if (StringUtils.equals(userProblemQuestion.getQuestionId(),
                    questionId)) {
                //Increase the wrong-answer count:
                userProblemQuestion.adjustCount(userAnswer.getResult());
            }
        }
    }

    private List<UserProblemQuestion> getProblemQuestionsListWithCreate(final String sectionId) {
        List<UserProblemQuestion> list = userProblemQuestions.get(sectionId);
        if (list == null) {
            // We use a LinkedList, instead of HashMap,
            // so that addUserAnswerAtStart() is more efficient.
            list = new LinkedList<>();
            userProblemQuestions.put(sectionId, list);
        } return list;
    }

    public List<UserProblemQuestion> getProblemQuestions(final String sectionId) {
        if (userProblemQuestions == null) {
            return null;
        }

        return userProblemQuestions.get(sectionId);    }

    public UserStats getStats(final String sectionId ) {
        return sectionStats.get(sectionId);
    }


    public QuizSections getSections() {
        return sections;
    }

    private UserStats getStatsWithAdd(final String userId, final String sectionId ) {
        UserStats stats = getStats(sectionId);
        if (stats == null) {
            stats = new UserStats(userId, sectionId);
            setStats(sectionId, stats);
        }

        return stats;
    }

    private void setStats(final String sectionId, final UserStats stats) {
        sectionStats.put(sectionId, stats);
    }
}

package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

import java.util.*;

/**
 * Created by murrayc on 1/23/16.
 */
public class UserRecentHistory implements IsSerializable {
    private QuizSections sections;
    private Map<String, List<UserAnswer>> userAnswers;

    UserRecentHistory() {

    }

    public UserRecentHistory(final QuizSections sections) {
        this.sections = sections;
    }


    public void setUserAnswers(final String sectionId, final List<UserAnswer> userAnswers) {
        final List<UserAnswer> list = getUserAnswersListWithCreate(sectionId);
        list.clear();
        list.addAll(userAnswers);
    }


    /**
     * Add @a userAnswer to the beginning of the list for it section, making sure that
     * there are no more than @max items in that sections's list. If necessary,
     * this removes older items.
     *
     * @param userAnswer
     * @param max
     */
    public void addUserAnswerAtStart(final UserAnswer userAnswer, final int max) {
        if (userAnswer == null) {
        }

        final List<UserAnswer> list = getUserAnswersListWithCreate(userAnswer.getSectionId());
        list.add(0, userAnswer);
        while(!list.isEmpty() && list.size() > max) {
            list.remove(list.size() - 1);
        }
    }

    private List<UserAnswer> getUserAnswersListWithCreate(final String sectionId) {
        if (userAnswers == null) {
            userAnswers = new HashMap<>();
        }

        List<UserAnswer> list = userAnswers.get(sectionId);
        if (list == null) {
            // We use a LinkedList , instead of HashMap,
            // so that addUserAnswerAtStart() is more efficient.
            list = new LinkedList<>();
            userAnswers.put(sectionId, list);
        } return list;
    }

    public List<UserAnswer> getUserAnswers(final String sectionId ) {
        if (userAnswers == null) {
            return null;
        }

        return userAnswers.get(sectionId);
    }


    public QuizSections getSections() {
        return sections;
    }


}

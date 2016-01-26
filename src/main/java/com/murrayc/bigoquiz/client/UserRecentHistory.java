package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by murrayc on 1/23/16.
 */
public class UserRecentHistory implements IsSerializable {
    private QuizSections sections;
    private Map<String, List<UserAnswer>> userAnswers;

    UserRecentHistory() {

    }

    public UserRecentHistory(final QuizSections sections, final Map<String, List<UserAnswer>> userAnswers) {
        this.sections = sections;
        this.userAnswers = userAnswers;
    }

    /*
    public void addUserAnswers(final String sectionId, final List<UserAnswer> userAnswers) {
        //TODO: This is inefficient if the list was null to begin with.
        final List<UserAnswer> list = getUserAnswersListWithCreate(sectionId);
        list.addAll(userAnswers);
    }
    */

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
            list = new ArrayList<>();
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

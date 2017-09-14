package com.murrayc.bigoquiz.client.application.userprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.UserHistoryOverall;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.application.userstatus.UserStatusView;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileView extends ContentViewWithUIHandlers<UserProfileUserEditUiHandlers>
        implements UserProfilePresenter.MyView {
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final UserStatusView userStatusView = new UserStatusView();
    final FlowPanel detailsPanel = new FlowPanel();

    private final PlaceManager placeManager;

    @Inject
    UserProfileView(final PlaceManager placeManager) {
        this.placeManager = placeManager;

        setTitle(constants.profileTitle());

        mainPanel.add(userStatusView);
        userStatusView.setShowLogOutWhenAppropriate(true);

        Utils.addHeaderToPanel(2, mainPanel, constants.historyTitle());
        mainPanel.add(detailsPanel);
        detailsPanel.addStyleName("user-status-answers-panel");
    }

    @Override
    public void setUserStatusFailed() {
        setErrorLabelVisible(true);
    }

    private void setLoginInfo(final LoginInfo loginInfo) {
        if (loginInfo == null) {
            Log.error("setLoginInfo(): loginInfo is null.");
            setErrorLabelVisible(true);
            return;
        }

        setErrorLabelVisible(false);
        userStatusView.setLoginInfo(loginInfo);
    }

    @Override
    public void setUserRecentHistory(final UserHistoryOverall result) {
        setLoginInfo(result == null ? null : result.getLoginInfo());
        buildUi(result);
    }

    private void buildUi(final UserHistoryOverall userHistoryOverall) {
        detailsPanel.clear();

        if (userHistoryOverall == null) {
            return;
        }

        @Nullable final Map<String, UserHistoryOverall.QuizDetails> quizzes = userHistoryOverall.getQuizzes();
        if (quizzes == null || quizzes.isEmpty()) {
            Utils.addParagraphWithText(detailsPanel, constants.quizHistoryPlaceholder(), "");
        }

        @NotNull final Set<String> quizIdsSet = quizzes.keySet();
        @NotNull List<String> quizIds = new ArrayList<>(quizIdsSet);
        quizIds.sort(generateQuizDetailsComparator(quizzes));

        for (@Nullable final String quizId : quizIds) {
            if (quizId == null) {
                continue;
            }

            if (StringUtils.isEmpty(quizId)) {
                continue;
            }

            final UserHistoryOverall.QuizDetails quizDetails = quizzes.get(quizId);
            if (quizDetails == null) {
                continue;
            }

            @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuizHistory(quizId);
            final String historyToken = placeManager.buildHistoryToken(placeRequest);

            final String quizTitle = messages.quizTitle(quizDetails.title);
            @NotNull final Hyperlink titleLabel = new InlineHyperlink(quizTitle, historyToken);
            //titleLabel.addStyleName("user-history-section-title-label");

            Utils.addHeaderToPanel(3, detailsPanel, titleLabel);

            final int questionsCount = quizDetails.questionsCount;

            final UserStats stats = userHistoryOverall.getStats(quizId);
            if (stats != null) {
                Utils.addStackedProgressBar(detailsPanel, stats.getCorrectOnce(), stats.getAnsweredOnce(), questionsCount, messages);
            } else {
                Log.error("buildUi(): UserStats is null.");
            }
        }
    }

    /**
     * A comparator, to sort a list of quiz IDs by the titles of the corresponding quizzes.
     */
    @NotNull
    private Comparator<String> generateQuizDetailsComparator(final Map<String, UserHistoryOverall.QuizDetails> quizzes) {
        return (o1, o2) -> {
            if ((o1 == null) && (o2 == null)) {
                return 0;
            } else if (o1 == null) {
                return -1;
            }

            final UserHistoryOverall.QuizDetails q1 = quizzes.get(o1);
            final UserHistoryOverall.QuizDetails q2 = quizzes.get(o2);

            if ((q1 == null) && (q2 == null)) {
                return 0;
            } else if (q1 == null) {
                return -1;
            }

            final String title1 = q1.title;
            final String title2 = q2.title;
            if ((title1 == null) && (title2 == null)) {
                return 0;
            } else if (title1 == null) {
                return -1;
            }

            return title1.compareTo(title2);
        };
    }

}

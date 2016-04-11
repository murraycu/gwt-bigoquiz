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
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileView extends ContentViewWithUIHandlers<UserProfileUserEditUiHandlers>
        implements UserProfilePresenter.MyView {
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Label usernameLabel = new InlineLabel();
    private final Anchor logoutLabel = new Anchor(constants.logOut());
    final FlowPanel detailsPanel = new FlowPanel();
    private final Button buttonResetSections = new Button(constants.buttonResetSections());

    private Panel loginParagraph = null;
    private final InlineHTML loginLabel = new InlineHTML();

    private final PlaceManager placeManager;

    @Inject
    UserProfileView(final PlaceManager placeManager) {
        this.placeManager = placeManager;

        setTitle(constants.profileTitle());

        //This is only visible when necessary:
        loginParagraph = Utils.addParagraphWithChild(mainPanel, loginLabel);
        loginParagraph.setVisible(false);

        Utils.addParagraphWithChild(mainPanel, usernameLabel);

        mainPanel.add(logoutLabel);
        logoutLabel.addStyleName("logout-label");

        Utils.addHeaderToPanel(2, mainPanel, constants.historyTitle());
        mainPanel.add(detailsPanel);
        detailsPanel.addStyleName("user-status-answers-panel");
    }

    @Override
    public void setUserStatusFailed() {
        setErrorLabelVisible(true);
    }

    private void setLoginInfo(final LoginInfo loginInfo) {
        //Defaults:
        usernameLabel.setVisible(false);
        logoutLabel.setVisible(false);
        buttonResetSections.setVisible(false);
        loginParagraph.setVisible(false);

        if (loginInfo == null) {
            Log.error("setLoginInfo(): loginInfo is null.");
            setErrorLabelVisible(true);
            return;
        }

        setErrorLabelVisible(false);

        if (loginInfo.isLoggedIn()) {
            usernameLabel.setVisible(true);
            logoutLabel.setVisible(true);
            buttonResetSections.setVisible(true);

            usernameLabel.setText(messages.username(loginInfo.getNickname()));
            logoutLabel.setHref(loginInfo.getLogoutUrl());
        } else {
            loginLabel.setHTML(messages.pleaseSignIn(loginInfo.getLoginUrl()));
            loginParagraph.setVisible(true);
        }
    }

    @Override
    public void setUserRecentHistory(final UserHistoryOverall result) {
        setLoginInfo(result == null ? null : result.getLoginInfo());
        buildUi(result);
    }

    private void buildUi(final UserHistoryOverall UserHistoryOverall) {
        detailsPanel.clear();

        if (!UserHistoryOverall.hasUser()) {
            final LoginInfo loginInfo = UserHistoryOverall.getLoginInfo();
            loginLabel.setHTML(messages.pleaseSignIn(loginInfo.getLoginUrl()));
            loginParagraph.setVisible(true);
        }

        @Nullable final Map<String, UserHistoryOverall.QuizDetails> quizzes = UserHistoryOverall.getQuizzes();
        if (quizzes == null || quizzes.isEmpty()) {
            Utils.addParagraphWithText(detailsPanel, constants.quizHistoryPlaceholder(), "");
        }

        @NotNull final Set<String> quizIds = quizzes.keySet();

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

            final UserStats stats = UserHistoryOverall.getStats(quizId);
            if (stats != null) {
                Utils.addStackedProgressBar(detailsPanel, stats.getCorrectOnce(), stats.getAnsweredOnce(), questionsCount, messages);
            } else {
                Log.error("buildUi(): UserStats is null.");
            }
        }
    }

}

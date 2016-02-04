package com.murrayc.bigoquiz.client.application.userstatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserStatusView extends ViewWithUiHandlers<UserStatusUserEditUiHandlers>
        implements UserStatusPresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

    private final Anchor usernameLabel = new Anchor();

    private final Panel loginPanel = new FlowPanel();
    private final Label loginFailedLabel = new Label(constants.errorNoServer());
    private final Anchor signInLink = new Anchor(constants.signInLinkTitle());

    @Nullable
    private LoginInfo loginInfo = null;
    @Nullable
    private UserProfile userProfile = null;
    private boolean loginServerFailed = false;

    UserStatusView() {
        @NotNull final FlowPanel statusPanel = new FlowPanel();
        statusPanel.addStyleName("status-panel");
        //box.getElement().setAttribute("id", "titlebox");
        statusPanel.add(usernameLabel);
        usernameLabel.addStyleName("user-name");

        @NotNull Label scoreLabel = new Label();
        statusPanel.add(scoreLabel);
        scoreLabel.addStyleName("score");

        loginPanel.addStyleName("login-panel");

        loginPanel.add(signInLink);
        signInLink.addStyleName("sign-in-link");

        loginPanel.add(loginFailedLabel);
        loginFailedLabel.addStyleName("login-failed");
        loginFailedLabel.setVisible(false);

        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-status-panel");
        mainPanel.add(loginPanel);
        mainPanel.add(statusPanel);
        initWidget(mainPanel);
    }

    private void showLogin() {
        if (loginInfo == null) {
            Log.error("showLogin(): loginInfo was null.");
        } else if (!loginInfo.isLoggedIn()) {
            signInLink.setHref(loginInfo.getLoginUrl());
            loginPanel.setVisible(true);
        } else {
            loginPanel.setVisible(false);
        }

        loginFailedLabel.setVisible(loginServerFailed);
    }

    private void showStatus() {
        //TODO: Avoid duplication with UserProfile.
        String username = loginInfo == null ? "" : loginInfo.getNickname();
        if (userProfile != null) {
            username = userProfile.getName();
        }

        usernameLabel.setText(username);
        usernameLabel.setHref("#" + NameTokens.USER_PROFILE);
    }

    @Override
    public void setUserStatus(final UserProfile userProfile) {
        this.userProfile = userProfile;
        this.loginServerFailed = false;


        updateUi();
    }

    @Override
    public void setUserStatusFailed() {
        this.userProfile = null;
        this.loginServerFailed = true;

        updateUi();
    }

    @Override
    public void setLoginInfo(LoginInfo result) {
        loginInfo = result;
        updateUi();
    }

    private void updateUi() {
        showLogin();
        showStatus();
    }
}

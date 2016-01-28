package com.murrayc.bigoquiz.client.application.userstatus;

import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserStatusView extends ViewWithUiHandlers<UserStatusUserEditUiHandlers>
        implements UserStatusPresenter.MyView {

    private final Anchor usernameLabel = new Anchor();
    private final Label scoreLabel = new Label();

    private final Panel loginPanel = new FlowPanel();
    private final Label loginFailedLabel = new Label(
            "Error: Could not connect to the login server.");
    private final Anchor signInLink = new Anchor("Sign In");

    private LoginInfo loginInfo = null;
    private UserProfile userProfile = null;
    private boolean loginServerFailed = false;

    UserStatusView() {
        final FlowPanel statusPanel = new FlowPanel();
        statusPanel.addStyleName("status-panel");
        //box.getElement().setAttribute("id", "titlebox");
        statusPanel.add(usernameLabel);
        usernameLabel.addStyleName("user-name");

        statusPanel.add(scoreLabel);
        scoreLabel.addStyleName("score");

        loginPanel.addStyleName("login-panel");

        loginPanel.add(signInLink);
        signInLink.addStyleName("sign-in-link");

        loginPanel.add(loginFailedLabel);
        loginFailedLabel.addStyleName("login-failed");
        loginFailedLabel.setVisible(false);

        final FlowPanel mainPanel = new FlowPanel();
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

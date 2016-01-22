package com.murrayc.bigoquiz.client.application.userstatus;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserStatusView extends ViewWithUiHandlers<UserEditUiHandlers>
        implements UserStatusPresenter.MyView {

    private final Label usernameLabel = new Label();
    private final Label scoreLabel = new Label();

    private final VerticalPanel loginPanel = new VerticalPanel();
    private final Label loginLabel = new Label(
            "Please sign in to your Google Account to track your progress.");
    private final Label loginFailedLabel = new Label(
            "Error: Could not connect to the login server.");
    private final Anchor signInLink = new Anchor("Sign In");

    private LoginInfo loginInfo = null;
    private UserProfile userProfile = null;
    private boolean loginServerFailed = false;

    UserStatusView() {
        final FlowPanel statusPanel = new FlowPanel();
        //box.getElement().setAttribute("id", "titlebox");
        statusPanel.add(usernameLabel);
        statusPanel.add(scoreLabel);

        loginPanel.add(loginLabel);
        loginPanel.add(signInLink);
        loginPanel.add(loginFailedLabel);
        loginFailedLabel.setVisible(false);

        final FlowPanel mainPanel = new FlowPanel();
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

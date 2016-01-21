package com.murrayc.bigoquiz.client.ui;

import com.google.gwt.user.client.ui.*;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/19/16.
 */
public class UserStatusViewImpl extends Composite implements UserStatusView {
    private Presenter presenter;

    private Label usernameLabel = new Label();
    private Label scoreLabel = new Label();

    private VerticalPanel loginPanel = new VerticalPanel();
    private Label loginLabel = new Label(
            "Please sign in to your Google Account to track your progress.");
    private Anchor signInLink = new Anchor("Sign In");

    private LoginInfo loginInfo = null;
    private UserProfile userProfile = null;

    public UserStatusViewImpl() {
        final FlowPanel statusPanel = new FlowPanel();
        //box.getElement().setAttribute("id", "titlebox");
        statusPanel.add(usernameLabel);
        statusPanel.add(scoreLabel);

        loginPanel.add(loginLabel);
        loginPanel.add(signInLink);

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.add(loginPanel);
        mainPanel.add(statusPanel);
        initWidget(mainPanel);
    }

    @Override
    public void setPresenter(final Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void clear() {

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

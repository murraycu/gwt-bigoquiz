package com.murrayc.bigoquiz.client.application.userstatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
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

    @Override
    public void setUserStatusFailed() {
        this.loginServerFailed = true;

        updateUi();
    }

    @Override
    public void setLoginInfo(@NotNull LoginInfo result) {
        loginInfo = result;
        updateUi();
    }

    private void updateUi() {
        //Login status:
        if (loginInfo == null) {
            Log.error("showLogin(): loginInfo was null.");
        } else if (!loginInfo.isLoggedIn()) {
            signInLink.setHref(loginInfo.getLoginUrl());
            loginPanel.setVisible(true);
        } else {
            loginPanel.setVisible(false);
        }

        loginFailedLabel.setVisible(loginServerFailed);

        final String username = loginInfo.getNickname();
        //TODO: If we ever let the user specify their own name just for our website:
        /*
        if (userProfile != null) {
            //TODO: This is
            username = userProfile.getName();
        } else {
            username = loginInfo.getNickname();
        }
        */

        usernameLabel.setText(username);
        usernameLabel.setHref("#" + NameTokens.USER_PROFILE);
    }
}

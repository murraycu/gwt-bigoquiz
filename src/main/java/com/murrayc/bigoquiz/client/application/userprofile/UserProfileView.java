package com.murrayc.bigoquiz.client.application.userprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.Utils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileView extends ContentViewWithUIHandlers<UserProfileUserEditUiHandlers>
        implements UserProfilePresenter.MyView {
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Label usernameLabel = new InlineLabel();
    private final Anchor logoutLabel = new Anchor(constants.logOut());
    private final Button buttonResetSections = new Button(constants.buttonResetSections());

    private Panel loginParagraph = null;
    private final InlineHTML loginLabel = new InlineHTML();

    UserProfileView() {
        setTitle(constants.profileTitle());

        //This is only visible when necessary:
        loginParagraph = Utils.addParagraphWithChild(mainPanel, loginLabel);
        loginParagraph.setVisible(false);

        Utils.addParagraphWithChild(mainPanel, usernameLabel);

        mainPanel.add(logoutLabel);
        logoutLabel.addStyleName("logout-label");
    }

    @Override
    public void setUserStatusFailed() {
        setErrorLabelVisible(true);
    }

    @Override
    public void setLoginInfo(@NotNull final LoginInfo loginInfo) {
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
}

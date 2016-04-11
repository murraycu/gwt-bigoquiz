package com.murrayc.bigoquiz.client.application.userstatus;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.ImageResources;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.application.Utils;
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

    private final Panel logoutPanel;
    private final Anchor logoutLabel = new Anchor(constants.logOut());

    @Nullable
    private LoginInfo loginInfo = null;
    private boolean loginServerFailed = false;
    private boolean showLogOutWhenAppropriate = false;

    public UserStatusView() {
        @NotNull final FlowPanel statusPanel = new FlowPanel();
        statusPanel.addStyleName("status-panel");
        //box.getElement().setAttribute("id", "titlebox");
        statusPanel.add(usernameLabel);
        usernameLabel.addStyleName("user-name");

        loginPanel.addStyleName("login-panel");

        final ImageResources images = GWT.create(ImageResources.class);

        PushButton signInButton = new PushButton(constants.signInLinkTitle());
        loginPanel.add(signInButton);
        signInButton.getUpFace().setImage(new Image(images.getGoogleSignInNormal()));
        signInButton.getDownFace().setImage(new Image(images.getGoogleSignInPressed()));
        signInButton.getUpHoveringFace().setImage(new Image(images.getGoogleSignInFocus()));
        //TODO: Set alt text for the button or its image: signInButton.setText(constants.signInLinkTitle());
        signInButton.removeStyleName("gwt-PushButton"); //We want the hover functionality, but not the borders and margins.
        signInButton.addStyleName("sign-in-button");
        signInButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                onSignInClicked();
            }
        });

        loginPanel.add(loginFailedLabel);
        loginFailedLabel.addStyleName("login-failed");
        loginFailedLabel.setVisible(false);

        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-status-panel");
        mainPanel.add(loginPanel);

        mainPanel.add(statusPanel);

        logoutPanel = Utils.addParagraphWithChild(mainPanel, logoutLabel);

        initWidget(mainPanel);
    }

    @Override
    public void setShowLogOutWhenAppropriate(boolean show) {
        this.showLogOutWhenAppropriate = show;
    }

    private void setLogOutVisibility() {
        boolean visible = false;
        if (this.showLogOutWhenAppropriate &&
                loginInfo != null && loginInfo.isLoggedIn()) {
            visible = true;
        }

        logoutPanel.setVisible(visible);
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

    private void onSignInClicked() {
        if (loginInfo == null) {
            Log.error("onSignInClicked(): loginInfo was null.");
            return;
        }

        Window.Location.assign(loginInfo.getLoginUrl());
    }

    private void updateUi() {
        //Login status:
        if (loginInfo == null) {
            Log.error("showLogin(): loginInfo was null.");
        } else if (!loginInfo.isLoggedIn()) {
            //signInLink.setHref();
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

        logoutLabel.setHref(loginInfo.getLogoutUrl());
        setLogOutVisibility();
    }
}

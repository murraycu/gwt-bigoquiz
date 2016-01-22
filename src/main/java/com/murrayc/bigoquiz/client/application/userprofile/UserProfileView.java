package com.murrayc.bigoquiz.client.application.userprofile;

import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.LoginInfo;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileView extends ViewWithUiHandlers<UserProfileUserEditUiHandlers>
        implements UserProfilePresenter.MyView {
    private Label usernameTitleLabel = new Label("Username: ");
    private Label usernameLabel = new Label();
    private final Anchor logoutLabel = new Anchor("Log out");

    UserProfileView() {
        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-profile-panel");

        final Label titleLabel = new Label("Profile");
        titleLabel.addStyleName("page-title-label");
        mainPanel.add(titleLabel);

        mainPanel.add(usernameTitleLabel);
        usernameTitleLabel.addStyleName("username-title-label");
        mainPanel.add(usernameLabel);
        usernameLabel.addStyleName("username-label");

        mainPanel.add(logoutLabel);
        logoutLabel.addStyleName("logout-label");

        initWidget(mainPanel);
    }


    @Override
    public void setUserStatusFailed() {

    }

    @Override
    public void setLoginInfo(final LoginInfo loginInfo) {
        String username = null;
        String logoutLink = null;
        if (loginInfo != null) {
            username = loginInfo.getNickname();
            logoutLink = loginInfo.getLogoutUrl();
        }

        usernameLabel.setText(username);
        logoutLabel.setHref(logoutLink);
    }
}

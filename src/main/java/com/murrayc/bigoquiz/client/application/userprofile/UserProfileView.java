package com.murrayc.bigoquiz.client.application.userprofile;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileView extends ViewWithUiHandlers<UserProfileUserEditUiHandlers>
        implements UserProfilePresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

    private final Label usernameLabel = new Label();
    private final Anchor logoutLabel = new Anchor(constants.logOut());

    UserProfileView() {
        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        Utils.addHeaderToPanel(2, mainPanel, constants.profileTitle());

        Label usernameTitleLabel = new Label(constants.username());
        mainPanel.add(usernameTitleLabel);
        usernameTitleLabel.addStyleName("username-title-label");
        mainPanel.add(usernameLabel);
        usernameLabel.addStyleName("username-label");

        mainPanel.add(logoutLabel);
        logoutLabel.addStyleName("logout-label");

        //Put it in a div with a specific class, so we can hide in on wider
        //screens where it is already visible in the sidebar:
        final SimplePanel userHistoryParent = new SimplePanel();
        userHistoryParent.addStyleName("user-profile-user-history-panel");
        mainPanel.add(userHistoryParent);

        final SimplePanel userHistoryRecentPanel = new SimplePanel();
        bindSlot(UserProfilePresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);
        userHistoryParent.add(userHistoryRecentPanel);

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

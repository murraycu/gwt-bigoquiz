package com.murrayc.bigoquiz.client;

import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/18/16.
 */
public class LoginInfo {
    private boolean loggedIn = false;
    private String loginUrl;
    private String logoutUrl;
    private String userId;
    private String nickname;
    private UserProfile userProfile;

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }

    public String getLoginUrl() {
        return loginUrl;
    }

    public void setLoginUrl(String loginUrl) {
        this.loginUrl = loginUrl;
    }

    public String getLogoutUrl() {
        return logoutUrl;
    }

    public void setLogoutUrl(String logoutUrl) {
        this.logoutUrl = logoutUrl;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setUserProfile(final UserProfile userProfile) {
        this.userProfile = userProfile;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(final String userId) {
        this.userId = userId;
    }
}


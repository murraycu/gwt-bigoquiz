package com.murrayc.bigoquiz.client.ui;

import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/19/16.
 */
public interface UserStatusView extends View {
    void setUserStatus(final UserProfile result);

    void setLoginInfo(final LoginInfo result);
}

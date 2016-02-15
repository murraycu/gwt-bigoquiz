package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginService;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/18/16.
 */
public class LoginServiceImpl extends ServiceWithUser implements
        LoginService {

    @NotNull
    public LoginInfo login(final String requestUri) {
        return getLoginInfo(requestUri);
    }
}

package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginService;

/**
 * Created by murrayc on 1/18/16.
 */
public class LoginServiceImpl extends RemoteServiceServlet implements
        LoginService {

    public LoginInfo login(final String requestUri) {
        final UserService userService = UserServiceFactory.getUserService();
        final User user = userService.getCurrentUser();
        final LoginInfo loginInfo = new LoginInfo();

        if (user != null) {
            loginInfo.setLoggedIn(true);
            loginInfo.setEmailAddress(user.getEmail());
            loginInfo.setNickname(user.getNickname());
            loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));
        } else {
            loginInfo.setLoggedIn(false);
            loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
        }
        return loginInfo;
    }

}

package com.murrayc.bigoquiz.server;

import com.allen_sauer.gwt.log.client.Log;
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
        final LoginInfo loginInfo = new LoginInfo();
        loginInfo.setLoggedIn(false);

        final UserService userService = UserServiceFactory.getUserService();
        if (userService == null) {
            return loginInfo;
        }

        User user = null;
        try {
            userService.getCurrentUser();
        } catch (final Exception ex) {
            //This happens when we run this in the gwt superdevmode,
            //instead of in the appengine.
            Log.error("Exception from userService.getCurrentUser()()", ex);
            return loginInfo;
        }


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

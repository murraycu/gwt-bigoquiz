package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.server.db.EntityManagerFactory;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/20/16.
 */
public class ServiceUserUtils {
    @Nullable
    public static User getUser() {
        final UserService userService = UserServiceFactory.getUserService();
        if (userService == null) {
            return null;
        }

        @Nullable User user = null;
        try {
            user = userService.getCurrentUser();
        } catch (@NotNull final Exception ex) {
            //This happens when we run this in the gwt superdevmode,
            //instead of in the appengine.
            Log.error("Exception from userService.getCurrentUser()()", ex);
            return null;
        }

        return user;
    }

    @NotNull
    public static LoginInfo getLoginInfo(final String requestUri) {
        @NotNull final LoginInfo loginInfo = new LoginInfo();
        loginInfo.setLoggedIn(false);

        @Nullable final User user = getUser();
        final UserService userService = UserServiceFactory.getUserService();
        if (user != null) {
            loginInfo.setLoggedIn(true);
            loginInfo.setUserId(user.getUserId());
            loginInfo.setNickname(user.getNickname());
            loginInfo.setLogoutUrl(userService.createLogoutURL(requestUri));

            //This is superfluous, but might contain more data one day:
            UserProfile userProfile = EntityManagerFactory.ofy().load().type(UserProfile.class).id(user.getUserId()).now();
            if (userProfile == null) {
                userProfile = new UserProfile(user.getUserId(), user.getNickname());
                EntityManagerFactory.ofy().save().entity(userProfile).now();
            }
            loginInfo.setUserProfile(userProfile);
        } else {
            loginInfo.setLoggedIn(false);

            if (userService != null) {
                loginInfo.setLoginUrl(userService.createLoginURL(requestUri));
            }
        }

        return loginInfo;
    }

    @NotNull
    public static UserProfile getUserProfileFromDataStore(final User user) {
        UserProfile userProfile = EntityManagerFactory.ofy().load().type(UserProfile.class).id(user.getUserId()).now();
        if (userProfile == null) {
            userProfile = new UserProfile(user.getUserId(), user.getNickname());
            EntityManagerFactory.ofy().save().entity(userProfile).now();
        }
        return userProfile;
    }
}

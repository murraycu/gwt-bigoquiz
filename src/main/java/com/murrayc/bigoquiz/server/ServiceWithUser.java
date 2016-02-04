package com.murrayc.bigoquiz.server;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.murrayc.bigoquiz.client.Log;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/20/16.
 */
public class ServiceWithUser extends RemoteServiceServlet {
    @Nullable
    protected User getUser() {
        final UserService userService = UserServiceFactory.getUserService();
        if (userService == null) {
            return null;
        }

        User user = null;
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
}

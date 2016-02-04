package com.murrayc.bigoquiz.server.db;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import com.murrayc.bigoquiz.shared.db.UserStats;

/**
 * Created by murrayc on 1/19/16.
 */
public class EntityManagerFactory {
    static {
        //Register classes whose instances we want to store in the database via Objectify:
        ObjectifyService.register(UserProfile.class);
        ObjectifyService.register(UserQuestionHistory.class);
        ObjectifyService.register(UserStats.class);
    }

    protected EntityManagerFactory() {
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }
}

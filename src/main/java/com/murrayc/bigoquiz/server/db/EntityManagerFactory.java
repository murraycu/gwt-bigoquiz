package com.murrayc.bigoquiz.server.db;

import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyService;
import com.murrayc.bigoquiz.shared.db.UserProblemQuestion;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import com.murrayc.bigoquiz.shared.db.UserStats;

/**
 * Created by murrayc on 1/19/16.
 */
public class EntityManagerFactory {
    private static EntityManagerFactory singleton;

    static {
        //Register classes whose instances we want to store in the database via Objectify:
        ObjectifyService.register(UserProfile.class);
        ObjectifyService.register(UserProblemQuestion.class);
        ObjectifyService.register(UserStats.class);
    }

    protected EntityManagerFactory() {
    }

    public static Objectify ofy() {
        return ObjectifyService.ofy();
    }

    public static EntityManagerFactory get() {
        if (singleton == null) {
            singleton = new EntityManagerFactory();
        }
        return singleton;
    }
}

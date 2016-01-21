package com.murrayc.bigoquiz.server.db;

import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.DAOBase;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/19/16.
 */
public class EntityManagerFactory extends DAOBase {
    private static EntityManagerFactory singleton;

    static {
        //Register classes whose instances we want to store in the databse via Objectify:
        ObjectifyService.register(UserProfile.class);
    }

    protected EntityManagerFactory() {
    }

    public static EntityManagerFactory get() {
        if (singleton == null) {
            singleton = new EntityManagerFactory();
        }
        return singleton;
    }
}

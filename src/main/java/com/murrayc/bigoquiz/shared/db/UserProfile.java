package com.murrayc.bigoquiz.shared.db;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by murrayc on 1/19/16.
 */
@Entity
public class UserProfile {
    /**
     * This is the same as the User.getUserId().
     */
    @Id
    private String id;

    private String name;

    // The email address that is associated the the user's Google account,
    // if the google account it associated with this user's bigoquiz account.
    private String googleEmail;

    UserProfile() {
    }

    /**
     * @param id The User ID, from User.getUserId().
     * @param name
     */
    public UserProfile(final String id, final String name) {
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getGoogleEmail() {
        return googleEmail;
    }

    public void setGoogleEmail(final String googleEmail) {
        this.googleEmail = googleEmail;
    }
}

package com.murrayc.bigoquiz.shared.db;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

/**
 * Created by murrayc on 1/19/16.
 */
@Entity
public class UserProfile implements IsSerializable {
    /**
     * This is the same as the User.getUserId().
     */
    @Id
    private String id;

    private long countCorrectAnswers;
    private String name;

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

    public long getCountCorrectAnswers() {
        return countCorrectAnswers;
    }

    public void setCountCorrectAnswers(long countCorrectAnswers) {
        this.countCorrectAnswers = countCorrectAnswers;
    }
}

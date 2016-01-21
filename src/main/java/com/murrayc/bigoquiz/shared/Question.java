package com.murrayc.bigoquiz.shared;

import com.google.gwt.user.client.rpc.IsSerializable;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class Question implements IsSerializable {
    private /* final */ String id;
    private /* final */ String text;
    private /* final */ List<String> choices;

    public Question() {
    }

    public Question(final String id, final String text, final List<String> choices) {
        this.id = id;
        this.text = text;
        this.choices = choices;
    }

    public String getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public List<String> getChoices() {
        return choices;
    }
}

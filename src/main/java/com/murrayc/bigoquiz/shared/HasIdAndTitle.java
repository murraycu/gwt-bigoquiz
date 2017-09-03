package com.murrayc.bigoquiz.shared;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Copyright (c) 2016 Murray Cumming
 *
 * Created by murrayc on 23.06.16.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class HasIdAndTitle {
    private String id;
    private String title;
    private String link;

    public HasIdAndTitle() {
    }

    public HasIdAndTitle(final String id, final String title, final String link) {
        this.id = id;
        this.title = title;
        this.link = link;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }
}

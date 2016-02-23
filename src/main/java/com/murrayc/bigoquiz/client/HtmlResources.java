package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/28/16.
 */
public interface HtmlResources extends ClientBundle {
    HtmlResources INSTANCE = GWT.create(HtmlResources.class);

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/reading.html")
    TextResource getReadingHtml();

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/sidebar-advert.html")
    TextResource getSidebarAdvertHtml();

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/about.html")
    TextResource getAboutHtml();

    @NotNull
    @Source("com/murrayc/bigoquiz/client/ui/home.html")
    TextResource getHomeHtml();
}

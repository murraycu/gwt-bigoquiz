package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.TextResource;

/**
 * Created by murrayc on 1/28/16.
 */
public interface HtmlResources extends ClientBundle {
    HtmlResources INSTANCE = GWT.create(HtmlResources.class);

    @Source("com/murrayc/bigoquiz/client/ui/reading.html")
    TextResource getReadingHtml();
}

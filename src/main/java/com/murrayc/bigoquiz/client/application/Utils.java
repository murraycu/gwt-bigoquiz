package com.murrayc.bigoquiz.client.application;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;

/**
 * Created by murrayc on 1/28/16.
 */
public class Utils {
    public static void addH2ToPanel(final Panel mainPanel, final String title) {
        final HeadingElement headingElement = Document.get().createHElement(2);
        headingElement.setInnerText(title);
        mainPanel.getElement().appendChild(headingElement);
    }

    public static Panel addParagraph(final Panel mainPanel) {
        final Panel p = new FlowPanel(ParagraphElement.TAG);
        mainPanel.add(p);
        return p;
    }

    public static Panel addParagraphWithChild(final FlowPanel mainPanel, final Label questionLabel) {
        final Panel p = addParagraph(mainPanel);
        p.add(questionLabel);
        return p;
    }
}

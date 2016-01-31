package com.murrayc.bigoquiz.client.application;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Created by murrayc on 1/28/16.
 */
public class Utils {
    public static HeadingElement addHeaderToPanel(int level, final Panel mainPanel, final String title) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        headingElement.setInnerText(title);
        mainPanel.getElement().appendChild(headingElement);
        return headingElement;
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

    public static boolean widgetIsVisible(final Widget w) {
        return w.isVisible() && (w.getAbsoluteLeft() > 0) && (w.getAbsoluteTop() > 0);
    }

    /* This doesn't seem to work:
    public static boolean widgetIsInvisible(Widget w) {
        while (w.getElement().hasParentElement()) {
            if (!w.isVisible()) {
                return false;
            }

            w = w.getParent();
        }

        return w.isVisible();
    }
    */
}

package com.murrayc.bigoquiz.client.application;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;

/**
 * Created by murrayc on 1/28/16.
 */
public class Utils {
    public static void addHeaderToPanel(int level, final Panel mainPanel, final String title) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        headingElement.setInnerText(title);
        mainPanel.getElement().appendChild(headingElement);
    }

    public static void addHeaderToPanel(int level, final Panel mainPanel, final Widget widget) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        mainPanel.getElement().appendChild(headingElement);

        DOM.insertChild(headingElement, widget.getElement(), 0);
    }

    public static Panel addParagraphWithText(final Panel parentPanel, final String text, final String styleName) {
        final Panel para = addParagraph(parentPanel);
        final Label label = new InlineLabel(text);
        para.add(label);
        para.addStyleName(styleName);
        return para;
    }

    public static Panel addParagraph(final Panel mainPanel) {
        final Panel p = new FlowPanel(ParagraphElement.TAG);
        mainPanel.add(p);
        return p;
    }

    public static Panel addParagraphWithChild(final FlowPanel mainPanel, final Widget childWidget) {
        final Panel p = addParagraph(mainPanel);
        p.add(childWidget);
        return p;
    }

    public static boolean widgetIsVisible(final Widget w) {
        return w.isVisible() && (w.getAbsoluteLeft() > 0) && (w.getAbsoluteTop() > 0);
    }

    public static void addHtmlToPanel(final Panel parentPanel, final TextResource textResource) {
        final HTML htmlPanel = new HTML();
        htmlPanel.setHTML(textResource.getText());
        final SimplePanel readingPanel = new SimplePanel();
        readingPanel.add(htmlPanel);
        parentPanel.add(readingPanel);
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

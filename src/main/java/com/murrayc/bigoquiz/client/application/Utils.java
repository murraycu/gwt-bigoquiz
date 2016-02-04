package com.murrayc.bigoquiz.client.application;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/28/16.
 */
public class Utils {
    public static void addHeaderToPanel(int level, @NotNull final Panel mainPanel, final String title) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        headingElement.setInnerText(title);
        mainPanel.getElement().appendChild(headingElement);
    }

    public static void addHeaderToPanel(int level, @NotNull final Panel mainPanel, @NotNull final Widget widget) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        mainPanel.getElement().appendChild(headingElement);

        DOM.insertChild(headingElement, widget.getElement(), 0);
    }

    @NotNull
    public static Panel addParagraphWithText(@NotNull final Panel parentPanel, final String text, final String styleName) {
        final Panel para = addParagraph(parentPanel);
        final Label label = new InlineLabel(text);
        para.add(label);
        para.addStyleName(styleName);
        return para;
    }

    @NotNull
    public static Panel addParagraph(@NotNull final Panel mainPanel) {
        final Panel p = new FlowPanel(ParagraphElement.TAG);
        mainPanel.add(p);
        return p;
    }

    @NotNull
    public static Panel addParagraphWithChild(@NotNull final FlowPanel mainPanel, final Widget childWidget) {
        final Panel p = addParagraph(mainPanel);
        p.add(childWidget);
        return p;
    }

    public static boolean widgetIsVisible(@NotNull final Widget w) {
        return w.isVisible() && (w.getAbsoluteLeft() > 0) && (w.getAbsoluteTop() > 0);
    }

    public static void addHtmlToPanel(@NotNull final Panel parentPanel, @NotNull final TextResource textResource) {
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

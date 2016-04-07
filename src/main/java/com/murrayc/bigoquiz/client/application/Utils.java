package com.murrayc.bigoquiz.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.dom.client.ParagraphElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.HasHandlers;
import com.google.gwt.resources.client.TextResource;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.*;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/28/16.
 */
public class Utils {
    public static HeadingElement addHeaderToPanel(int level, @NotNull final Panel mainPanel, final String title) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        headingElement.setInnerText(title);
        mainPanel.getElement().appendChild(headingElement);
        return headingElement;
    }

    public static HeadingElement addHeaderToPanel(int level, @NotNull final Panel mainPanel, @NotNull final Widget widget) {
        final HeadingElement headingElement = Document.get().createHElement(level);
        mainPanel.getElement().appendChild(headingElement);

        DOM.insertChild(headingElement, widget.getElement(), 0);
        return headingElement;
    }

    @NotNull
    public static Panel addParagraphWithText(@NotNull final Panel parentPanel, final String text, final String styleName) {
        @NotNull final Panel para = addParagraph(parentPanel, styleName);
        @NotNull final Label label = new InlineLabel(text);
        para.add(label);
        return para;
    }

    @NotNull
    public static Panel addParagraph(@NotNull final Panel mainPanel, final String styleName) {
        @NotNull final Panel p = new FlowPanel(ParagraphElement.TAG);
        if (!StringUtils.isEmpty(styleName)) {
            p.addStyleName(styleName);
        }
        mainPanel.add(p);
        return p;
    }

    @NotNull
    public static Panel addParagraphWithChild(@NotNull final Panel mainPanel, final Widget childWidget) {
        @NotNull final Panel p = addParagraph(mainPanel, null);
        p.add(childWidget);
        return p;
    }

    public static boolean widgetIsVisible(@NotNull final Widget w) {
        return w.isVisible() && (w.getAbsoluteLeft() > 0) && (w.getAbsoluteTop() > 0);
    }

    public static void addHtmlToPanel(@NotNull final Panel parentPanel, @NotNull final TextResource textResource) {
        @NotNull final HTML htmlPanel = new HTML();
        htmlPanel.setHTML(textResource.getText());
        @NotNull final SimplePanel readingPanel = new SimplePanel();
        readingPanel.add(htmlPanel);
        parentPanel.add(readingPanel);
    }

    /**
     * Creates a Label with generic server error text,
     * to show if the communication with the server failed.
     * It will initially be invisible.
     *
     * @return
     * @param constants
     */
    @NotNull
    public static InlineLabel createServerErrorLabel(final BigOQuizConstants constants) {
        final InlineLabel result = new InlineLabel(constants.errorNoServer());
        result.addStyleName("server-error-label");
        result.setVisible(false);
        return result;
    }

    /**
     * Creates a Label with generic loading text,
     * to show that communication with the server is in progress.
     * It will initially be invisible.
     *
     * @return
     * @param constants
     */
    @NotNull
    public static InlineLabel createServerLoadingLabel(final BigOQuizConstants constants) {
        final InlineLabel result = new InlineLabel(constants.loading());
        result.addStyleName("server-loading-label");
        result.setVisible(false);
        return result;
    }

    public static void showResetConfirmationDialog(final ClickHandler okClickHandler) {
        @NotNull final DialogBox dialog = new DialogBox();
        dialog.setGlassEnabled(true);
        dialog.setAnimationEnabled(true);

        final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

        dialog.setText(constants.dialogResetSectionsTitle());

        @NotNull final Button buttonOK = new Button(constants.dialogResetSectionsOkButton());
        buttonOK.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                dialog.hide();

                okClickHandler.onClick(event);
            }
        });

        @NotNull final Button buttonCancel = new Button(constants.dialogResetSectionsCancelButton());
        buttonCancel.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                dialog.hide();
            }
        });

        @NotNull final Panel panelDialog = new FlowPanel();
        addParagraphWithText(panelDialog, constants.dialogResetSectionsText(),
                "reset-userhistorysections-confirm-dialog-text");
        @NotNull final Panel panelButtons = new FlowPanel();
        panelButtons.addStyleName("reset-sections-confirm-dialog-buttons-panel");
        panelButtons.add(buttonOK);
        buttonOK.addStyleName("reset-sections-confirm-dialog-ok-button");
        panelButtons.add(buttonCancel);
        buttonCancel.addStyleName("reset-sections-confirm-dialog-cancel-button");
        panelDialog.add(panelButtons);
        dialog.setWidget(panelDialog);

        dialog.center();
        dialog.show();
    }

    public static void tellUserHistoryPresenterAboutNoQuestionContext(final HasHandlers source) {
        QuestionContextEvent.fire(source, null, null, true);
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

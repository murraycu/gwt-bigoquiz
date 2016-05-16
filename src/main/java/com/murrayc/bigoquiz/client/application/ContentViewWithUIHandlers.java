package com.murrayc.bigoquiz.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.InlineLabel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 2/17/16.
 */
public class ContentViewWithUIHandlers<C extends UiHandlers> extends ViewWithUiHandlers<C>
        implements ContentView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    protected final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

    @NotNull
    protected Panel parentPanel = new FlowPanel();
    @NotNull
    protected Panel mainPanel = new FlowPanel();
    @NotNull final HeadingElement titleHeading;
    @NotNull final Label titleLabel = new InlineLabel();
    private final Label labelLoading = Utils.createServerLoadingLabel(constants);
    private final Label labelError = Utils.createServerErrorLabel(constants);

    public ContentViewWithUIHandlers() {
        parentPanel.addStyleName("parent-content-panel");
        mainPanel.addStyleName("content-panel");
        parentPanel.add(mainPanel);

        titleLabel.addStyleName("page-title-label");
        titleHeading = Utils.addHeaderToPanel(2, mainPanel, titleLabel);
        setHeadingVisible(false);

        mainPanel.add(labelLoading);
        setLoadingLabelVisible(false);
        mainPanel.add(labelError);
        setErrorLabelVisible(false);

        initWidget(parentPanel);
    }

    public void setTitle(final String title) {
        titleLabel.setText(title);
        setHeadingVisible(!StringUtils.isEmpty(title));

        //A good default:
        Window.setTitle(title);
    }

    @Override
    public void setLoadingLabelVisible(boolean visible) {
        labelLoading.setVisible(visible);
    }

    protected void setErrorLabel(final String message) {
        labelError.setText(message);
    }

    protected void setErrorLabelVisible(boolean visible) {
        labelError.setVisible(visible);
    }

    protected void setHeadingVisible(boolean visible) {
        titleHeading.getStyle().setProperty("display", visible ? "block" : "none");
    }

    @Override
    public void setServerFailed() {
        setErrorLabel(constants.errorNoServer());
        setErrorLabelVisible(true);
    }

    @Override
    public void setServerFailedUnknownQuiz() {
        setErrorLabel(constants.errorUnknownQuiz());
        setErrorLabelVisible(true);
    }
}

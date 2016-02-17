package com.murrayc.bigoquiz.client.application;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.UiHandlers;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 2/17/16.
 */
public class ContentViewWithUIHandlers<C extends UiHandlers> extends ViewWithUiHandlers<C> {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    protected final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

    @NotNull
    protected Panel mainPanel = new FlowPanel();
    @NotNull final Label titleLabel = new Label();
    private final Label labelError = Utils.createServerErrorLabel(constants);

    public ContentViewWithUIHandlers() {
        mainPanel.addStyleName("content-panel");
        titleLabel.addStyleName("page-title-label");
        mainPanel.add(titleLabel);

        mainPanel.add(labelError);
        initWidget(mainPanel);
    }

    public void setTitle(final String title) {
        titleLabel.setText(title);
    }

    protected void setErrorLabelVisible(boolean visible) {
        labelError.setVisible(visible);
    }
}

package com.murrayc.bigoquiz.client.application.about;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.HtmlResources;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class AboutView extends ViewWithUiHandlers<AboutUserEditUiHandlers>
        implements AboutPresenter.MyView {

    AboutView() {
        // BigOQuizConstants.java is generated in the target/ directory,
        // from BigOQuizConstants.properties
        // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
        final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

        @NotNull Panel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        @NotNull final Label titleLabel = new Label(constants.aboutTitle());
        titleLabel.addStyleName("page-title-label");
        mainPanel.add(titleLabel);

        Utils.addHtmlToPanel(mainPanel, HtmlResources.INSTANCE.getAboutHtml());

        @NotNull Label versionLabel = new Label();
        mainPanel.add(versionLabel);
        versionLabel.addStyleName("version-label");

        //TODO: Internationalization:
        //TODO: Get the number from pom.xml somehow.
        versionLabel.setText("Version: " + "0.9.4");

        initWidget(mainPanel);
    }
}

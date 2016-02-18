package com.murrayc.bigoquiz.client.application.about;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Label;
import com.murrayc.bigoquiz.client.HtmlResources;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class AboutView extends ContentViewWithUIHandlers<AboutUserEditUiHandlers>
        implements AboutPresenter.MyView {

    AboutView() {
        // BigOQuizConstants.java is generated in the target/ directory,
        // from BigOQuizConstants.properties
        // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
        final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
        setTitle(constants.aboutTitle());

        Utils.addHtmlToPanel(mainPanel, HtmlResources.INSTANCE.getAboutHtml());

        @NotNull Label versionLabel = new Label();
        mainPanel.add(versionLabel);
        versionLabel.addStyleName("version-label");

        //TODO: Internationalization:
        //TODO: Get the number from pom.xml somehow.
        versionLabel.setText("Version: " + "0.9.6");
    }
}

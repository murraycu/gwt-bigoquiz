package com.murrayc.bigoquiz.client.application.about;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Panel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Created by murrayc on 1/21/16.
 */
public class AboutView extends ViewWithUiHandlers<AboutUserEditUiHandlers>
        implements AboutPresenter.MyView {

    private final Panel mainPanel = new FlowPanel();
    private final Label aboutLabel = new Label("Yadda yadda yadda yadda.");
    private final Label versionLabel = new Label();

    AboutView() {
        mainPanel.addStyleName("content-panel");

        final Label titleLabel = new Label("About");
        titleLabel.addStyleName("page-title-label");
        mainPanel.add(titleLabel);

        mainPanel.add(aboutLabel);
        aboutLabel.addStyleName("about-label");

        mainPanel.add(versionLabel);
        versionLabel.addStyleName("version-label");

        //TODO: Internationalization:
        //TODO: Get the number from pom.xml somehow.
        versionLabel.setText("Version: " + "0.3");

        initWidget(mainPanel);
    }
}

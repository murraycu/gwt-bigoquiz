package com.murrayc.bigoquiz.client.application.about;

import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Created by murrayc on 1/21/16.
 */
public class AboutView extends ViewWithUiHandlers<AboutUserEditUiHandlers>
        implements AboutPresenter.MyView {

    private final VerticalPanel mainPanel = new VerticalPanel();
    private final Label aboutLabel = new Label("Yadda yadda yadda yadda.");

    AboutView() {
        mainPanel.add(aboutLabel);
        aboutLabel.addStyleName("about-label");

        initWidget(mainPanel);
    }
}

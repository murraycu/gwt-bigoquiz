package com.murrayc.bigoquiz.client.application.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;

/**
 * Created by murrayc on 1/21/16.
 */
public class MenuView extends ViewWithUiHandlers<MenuUserEditUiHandlers>
        implements MenuPresenter.MyView {

    // OnlineGlomConstants.java is generated in the target/ directory,
    // from OnlineGlomConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

    private final Anchor titleLabel = new Anchor(constants.appTitle());
    private final SimplePanel userStatusPanel = new SimplePanel();

    MenuView() {
        final Panel mainPanel = new FlowPanel();
        mainPanel.addStyleName("menu-panel");
        mainPanel.addStyleName("clearfix"); //So it is as high as its children.
        mainPanel.add(titleLabel);
        titleLabel.addStyleName("menu-title");
        titleLabel.setHref("#" + NameTokens.QUESTION); //TODO: Or just / ?

        Anchor aboutLink = new Anchor(constants.aboutTitle());
        aboutLink.setHref("#" + NameTokens.ABOUT);
        mainPanel.add(aboutLink);
        aboutLink.addStyleName("about-link");

        mainPanel.add(userStatusPanel);
        bindSlot(MenuPresenter.SLOT_USER_STATUS, userStatusPanel);

        initWidget(mainPanel);

    }

    private void goTo(final String token) {
        getUiHandlers().goTo(token);
    }
}

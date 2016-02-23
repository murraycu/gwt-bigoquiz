package com.murrayc.bigoquiz.client.application.menu;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class MenuView extends ViewWithUiHandlers<MenuUserEditUiHandlers>
        implements MenuPresenter.MyView {

    MenuView() {
        // BigOQuizConstants.java is generated in the target/ directory,
        // from BigOQuizConstants.properties
        // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
        final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);

        @NotNull final Panel mainPanel = new FlowPanel();
        mainPanel.addStyleName("menu-panel");
        mainPanel.addStyleName("clearfix"); //So it is as high as its children.
        @NotNull Anchor titleLabel = new Anchor(constants.appTitle());
        mainPanel.add(titleLabel);
        titleLabel.addStyleName("menu-title");
        titleLabel.addStyleName("clearfix"); //Stop any other item from ever being in the rectangle of the title text.
        titleLabel.setHref(""); //Takes us to the main entry point (/).

        @NotNull final Panel othersPanel = new FlowPanel();
        othersPanel.addStyleName("menu-others-panel");
        othersPanel.addStyleName("clearfix");
        mainPanel.add(othersPanel);
        @NotNull Anchor aboutLink = new Anchor(constants.aboutTitle());
        aboutLink.setHref("#" + NameTokens.ABOUT);
        othersPanel.add(aboutLink);
        aboutLink.addStyleName("about-link");

        @NotNull SimplePanel userStatusPanel = new SimplePanel();
        othersPanel.add(userStatusPanel);
        bindSlot(MenuPresenter.SLOT_USER_STATUS, userStatusPanel);

        initWidget(mainPanel);

    }
}

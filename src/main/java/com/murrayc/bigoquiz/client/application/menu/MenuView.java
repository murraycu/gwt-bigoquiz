package com.murrayc.bigoquiz.client.application.menu;

import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.ui.*;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.NameTokens;

/**
 * Created by murrayc on 1/21/16.
 */
public class MenuView extends ViewWithUiHandlers<MenuUserEditUiHandlers>
        implements MenuPresenter.MyView {

    private final Anchor titleLabel = new Anchor("Big-O Algorithms Quiz");
    private final SimplePanel userStatusPanel = new SimplePanel();

    MenuView() {
        final Panel mainPanel = new FlowPanel();
        mainPanel.addStyleName("menu-panel");
        mainPanel.addStyleName("clearfix"); //So it is as high as its children.
        mainPanel.add(titleLabel);
        titleLabel.addStyleName("menu-title");
        titleLabel.setHref("#" + NameTokens.QUESTION); //TODO: Or just / ?

        Anchor aboutLink = new Anchor("About");
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

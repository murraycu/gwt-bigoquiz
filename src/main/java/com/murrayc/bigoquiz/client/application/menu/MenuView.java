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
    private final MenuBar menuBar = new MenuBar();

    MenuView() {
        final Panel mainPanel = new FlowPanel();
        mainPanel.addStyleName("menu-panel");
        mainPanel.add(titleLabel);
        titleLabel.addStyleName("menu-title");
        titleLabel.setHref("#" + NameTokens.QUESTION); //TODO: Or just / ?

        mainPanel.add(menuBar);
        menuBar.addStyleName("menu-bar");

        //Instead, the title is a clickable link.
        /*
        menuBar.addItem("Home", new Command() {
            @Override
            public void execute() {
                goTo(NameTokens.QUESTION);
            }
        });
        */

        //Instead, the user name is a clickable link:
        /*
        menuBar.addItem("Profile", new Command() {
            @Override
            public void execute() {
                goTo(NameTokens.USER_PROFILE);
            }
        });
        */

        menuBar.addItem("About", new Command() {
            @Override
            public void execute() {
                goTo(NameTokens.ABOUT);
            }
        });

        initWidget(mainPanel);
    }

    private void goTo(final String token) {
        getUiHandlers().goTo(token);
    }
}

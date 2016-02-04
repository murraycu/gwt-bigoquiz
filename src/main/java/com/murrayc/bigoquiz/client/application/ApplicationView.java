package com.murrayc.bigoquiz.client.application;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.murrayc.bigoquiz.client.HtmlResources;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {

    ApplicationView() {
        @NotNull SimplePanel menuPanel = new SimplePanel();
        @NotNull Panel main = new FlowPanel();
        main.add(menuPanel);
        @NotNull SimplePanel contentPanel = new SimplePanel();
        main.add(contentPanel);


        //We use a CSS media query to only show this on wider screens:
        @NotNull Panel sidebarPanelLinks = new FlowPanel();
        main.add(sidebarPanelLinks);
        sidebarPanelLinks.addStyleName("sidebar-panel-links");


        //We use a CSS media query to only show this on wider screens:
        @NotNull Panel sidebarPanelSections = new FlowPanel();
        main.add(sidebarPanelSections);
        sidebarPanelSections.addStyleName("sidebar-panel-sections");

        @NotNull SimplePanel userHistoryRecentPanel = new SimplePanel();
        sidebarPanelSections.add(userHistoryRecentPanel);


        //Add some static HTML in the sidebar:
        //TODO: It would be nicer to have this right in the main .html file,
        //but then we couldn't put it in the correct <div>.
        Utils.addHtmlToPanel(sidebarPanelLinks, HtmlResources.INSTANCE.getReadingHtml());
        Utils.addHtmlToPanel(sidebarPanelLinks, HtmlResources.INSTANCE.getSidebarAdvertHtml());


        initWidget(main);

        bindSlot(ApplicationPresenter.SLOT_MENU, menuPanel);
        bindSlot(ApplicationPresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);
        bindSlot(ApplicationPresenter.SLOT_CONTENT, contentPanel);
    }

}
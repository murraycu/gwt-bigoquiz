package com.murrayc.bigoquiz.client.application;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtplatform.mvp.client.ViewImpl;
import com.murrayc.bigoquiz.client.HtmlResources;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {

    ApplicationView() {
        SimplePanel menuPanel = new SimplePanel();
        Panel main = new FlowPanel();
        main.add(menuPanel);
        SimplePanel contentPanel = new SimplePanel();
        main.add(contentPanel);

        //We use a CSS media query to only show this on wider screens:
        Panel sidebarPanel = new FlowPanel();
        main.add(sidebarPanel);
        sidebarPanel.addStyleName("sidebar-panel");

        //Add some static HTML in the sidebar:
        //TODO: It would be nicer to have this right in the main .html file,
        //but then we couldn't put it in the correct <div>.
        final HTML htmlPanel = new HTML();
        final String html = HtmlResources.INSTANCE.getReadingHtml().getText();
        htmlPanel.setHTML(html);
        SimplePanel readingPanel = new SimplePanel();
        readingPanel.add(htmlPanel);
        sidebarPanel.add(readingPanel);

        SimplePanel userHistoryRecentPanel = new SimplePanel();
        sidebarPanel.add(userHistoryRecentPanel);

        initWidget(main);

        bindSlot(ApplicationPresenter.SLOT_MENU, menuPanel);
        bindSlot(ApplicationPresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);
        bindSlot(ApplicationPresenter.SLOT_CONTENT, contentPanel);
    }
}
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
    private final Panel main = new FlowPanel();
    private final SimplePanel mainPanel = new SimplePanel();
    private final SimplePanel readingPanel = new SimplePanel();
    private final SimplePanel menuPanel = new SimplePanel();
    private final SimplePanel userStatusPanel = new SimplePanel();
    private final SimplePanel userHistoryRecentPanel = new SimplePanel();

    ApplicationView() {
        main.add(menuPanel);

        //Add some static HTML:
        //TODO: It would be nicer to have this right in the main .html file,
        //but then we couldn't put it in the correct <div>.
        final HTML htmlPanel = new HTML();
        final String html = HtmlResources.INSTANCE.getReadingHtml().getText();
        htmlPanel.setHTML(html);
        readingPanel.add(htmlPanel);
        main.add(readingPanel);

        main.add(userStatusPanel);

        //We use a CSS media query to only show this on wider screens:
        main.add(userHistoryRecentPanel);
        userHistoryRecentPanel.addStyleName("user-history-recent-as-sidebar");

        main.add(mainPanel);
        initWidget(main);

        bindSlot(ApplicationPresenter.SLOT_MENU, menuPanel);
        bindSlot(ApplicationPresenter.SLOT_USER_STATUS, userStatusPanel);
        bindSlot(ApplicationPresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);
        bindSlot(ApplicationPresenter.SLOT_MAIN, mainPanel);
    }
}
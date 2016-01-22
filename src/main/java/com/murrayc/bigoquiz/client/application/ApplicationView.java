package com.murrayc.bigoquiz.client.application;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Panel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
    private final Panel main = new FlowPanel();
    private final SimplePanel mainPanel = new SimplePanel();
    private final SimplePanel menuPanel = new SimplePanel();
    private final SimplePanel userStatusPanel = new SimplePanel();


    ApplicationView() {
        main.add(menuPanel);
        main.add(userStatusPanel);
        main.add(mainPanel);
        initWidget(main);

        bindSlot(ApplicationPresenter.SLOT_MENU, menuPanel);
        bindSlot(ApplicationPresenter.SLOT_USER_STATUS, userStatusPanel);
        bindSlot(ApplicationPresenter.SLOT_MAIN, mainPanel);
    }
}
package com.murrayc.bigoquiz.client.application;

import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
    private final VerticalPanel main = new VerticalPanel();
    private final SimplePanel mainPanel = new SimplePanel();
    private final SimplePanel userStatusPanel = new SimplePanel();


    ApplicationView() {
        main.add(userStatusPanel);
        main.add(mainPanel);
        initWidget(main);

        bindSlot(ApplicationPresenter.SLOT_MAIN, mainPanel);
        bindSlot(ApplicationPresenter.SLOT_USER_STATUS, userStatusPanel);
    }
}
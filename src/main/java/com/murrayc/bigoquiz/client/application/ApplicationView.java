package com.murrayc.bigoquiz.client.application;

import com.google.gwt.user.client.ui.SimplePanel;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationView extends ViewImpl implements ApplicationPresenter.MyView {
    private final SimplePanel main;

    ApplicationView() {
        main = new SimplePanel();

        initWidget(main);
        bindSlot(ApplicationPresenter.SLOT_MAIN, main);
    }
}
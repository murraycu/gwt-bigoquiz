package com.murrayc.bigoquiz.client.application.home;

import com.google.inject.Inject;

import com.google.gwt.user.client.ui.Label;
import com.gwtplatform.mvp.client.ViewImpl;

/**
 * Created by murrayc on 1/21/16.
 */
public class HomeView extends ViewImpl implements HomePresenter.MyView {
    HomeView() {
        initWidget(new Label("Hello World!"));
    }
}

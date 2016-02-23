package com.murrayc.bigoquiz.client.application.home;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class HomeModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(HomePresenter.class, HomePresenter.MyView.class, HomeView.class,
                HomePresenter.MyProxy.class);    }
}

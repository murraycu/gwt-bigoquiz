package com.murrayc.bigoquiz.client.application.about;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class AboutModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(AboutPresenter.class, AboutPresenter.MyView.class, AboutView.class,
                AboutPresenter.MyProxy.class);    }
}

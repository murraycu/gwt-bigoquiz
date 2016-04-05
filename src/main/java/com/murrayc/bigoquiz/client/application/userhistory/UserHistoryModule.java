package com.murrayc.bigoquiz.client.application.userhistory;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(UserHistoryPresenter.class, UserHistoryPresenter.MyView.class, UserHistoryView.class,
                UserHistoryPresenter.MyProxy.class);
    }
}

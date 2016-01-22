package com.murrayc.bigoquiz.client.application.userprofile;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(UserProfilePresenter.class, UserProfilePresenter.MyView.class, UserProfileView.class,
                UserProfilePresenter.MyProxy.class);
    }
}

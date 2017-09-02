package com.murrayc.bigoquiz.client.application.login;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class LoginModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(LoginPresenter.class, LoginPresenter.MyView.class, LoginView.class,
                LoginPresenter.MyProxy.class);
    }
}

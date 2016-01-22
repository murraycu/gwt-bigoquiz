package com.murrayc.bigoquiz.client.application;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.murrayc.bigoquiz.client.application.question.QuestionModule;
import com.murrayc.bigoquiz.client.application.userstatus.UserStatusModule;
import com.murrayc.bigoquiz.client.application.userstatus.UserStatusPresenter;
import com.murrayc.bigoquiz.client.application.userstatus.UserStatusView;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new QuestionModule());
        install(new UserStatusModule());

        bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class, ApplicationView.class,
                ApplicationPresenter.MyProxy.class);

        bindSingletonPresenterWidget(UserStatusPresenter.class, UserStatusPresenter.MyView.class,
                UserStatusView.class);
    }
}

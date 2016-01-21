package com.murrayc.bigoquiz.client.application;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.murrayc.bigoquiz.client.application.question.QuestionModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new QuestionModule());

        bindPresenter(ApplicationPresenter.class, ApplicationPresenter.MyView.class, ApplicationView.class,
                ApplicationPresenter.MyProxy.class);
    }
}

package com.murrayc.bigoquiz.client.application.question;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuestionModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(QuestionPresenter.class, QuestionPresenter.MyView.class, QuestionView.class,
                QuestionPresenter.MyProxy.class);
    }
}

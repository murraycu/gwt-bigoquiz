package com.murrayc.bigoquiz.client.application.quiz;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(QuizPresenter.class, QuizPresenter.MyView.class, QuizView.class,
                QuizPresenter.MyProxy.class);
    }
}

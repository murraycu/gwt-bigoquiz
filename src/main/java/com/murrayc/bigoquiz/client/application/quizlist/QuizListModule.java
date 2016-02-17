package com.murrayc.bigoquiz.client.application.quizlist;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizListModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(QuizListPresenter.class, QuizListPresenter.MyView.class, QuizListView.class,
                QuizListPresenter.MyProxy.class);
    }
}

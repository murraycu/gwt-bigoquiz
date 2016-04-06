package com.murrayc.bigoquiz.client.application.userhistory;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsPresenter;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsView;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(UserHistoryPresenter.class, UserHistoryPresenter.MyView.class, UserHistoryView.class,
                UserHistoryPresenter.MyProxy.class);

        bindPresenterWidget(UserHistorySectionsPresenter.class, UserHistorySectionsPresenter.MyView.class,
                UserHistorySectionsView.class);
    }
}

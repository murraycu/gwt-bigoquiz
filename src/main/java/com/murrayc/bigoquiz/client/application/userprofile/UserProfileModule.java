package com.murrayc.bigoquiz.client.application.userprofile;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.murrayc.bigoquiz.client.application.userhistoryrecent.UserHistoryRecentPresenter;
import com.murrayc.bigoquiz.client.application.userhistoryrecent.UserHistoryRecentView;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(UserProfilePresenter.class, UserProfilePresenter.MyView.class, UserProfileView.class,
                UserProfilePresenter.MyProxy.class);

        bindPresenterWidget(UserHistoryRecentPresenter.class, UserHistoryRecentPresenter.MyView.class,
                UserHistoryRecentView.class);
    }
}

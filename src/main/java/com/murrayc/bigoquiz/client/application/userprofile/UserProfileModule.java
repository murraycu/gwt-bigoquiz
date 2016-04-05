package com.murrayc.bigoquiz.client.application.userprofile;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsPresenter;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsView;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfileModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        bindPresenter(UserProfilePresenter.class, UserProfilePresenter.MyView.class, UserProfileView.class,
                UserProfilePresenter.MyProxy.class);

        bindPresenterWidget(UserHistorySectionsPresenter.class, UserHistorySectionsPresenter.MyView.class,
                UserHistorySectionsView.class);
    }
}

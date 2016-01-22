package com.murrayc.bigoquiz.client.application.userstatus;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginServiceAsync;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserStatusPresenter extends Presenter<UserStatusPresenter.MyView, UserStatusPresenter.MyProxy>
        implements UserEditUiHandlers {
    interface MyView extends View, HasUiHandlers<UserEditUiHandlers> {
        void setUserStatus(final UserProfile result);

        void setLoginInfo(final LoginInfo result);
    }

    @ProxyStandard
    @NameToken(NameTokens.USER_STATUS)
    interface MyProxy extends ProxyPlace<UserStatusPresenter> {
    }

    @Inject
    UserStatusPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_MAIN);

        getView().setUiHandlers(this);

        final AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: login(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final UserProfile result) {
                getView().setUserStatus(result);
            }
        };

        QuizServiceAsync.Util.getInstance().getUserProfile(callback);


        // Check login status using login service.
        LoginServiceAsync.Util.getInstance().login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
            public void onFailure(final Throwable error) {
                //TODO: Handle this: Log.error("login() failed.", error);
            }

            public void onSuccess(final LoginInfo result) {
                getView().setLoginInfo(result);
            }
        });
    }
}
package com.murrayc.bigoquiz.client.application.userstatus;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginServiceAsync;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.shared.db.UserProfile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserStatusPresenter extends PresenterWidget<UserStatusPresenter.MyView>
        implements UserStatusUserEditUiHandlers {
    public interface MyView extends View, HasUiHandlers<UserStatusUserEditUiHandlers> {
        void setUserStatus(final UserProfile result);

        void setLoginInfo(final LoginInfo result);

        void setUserStatusFailed();
    }

    @Inject
    UserStatusPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        getView().setUiHandlers(this);

        final AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: getUserProfile(): " + caught.getMessage());
                getView().setUserStatusFailed();
            }

            @Override
            public void onSuccess(@Nullable final UserProfile result) {
                //TODO: Throw an exception instead of returning null?
                if(result == null) {
                    //getView().setServerFailed();
                } else {
                    getView().setUserStatus(result);
                }
            }
        };

        QuizServiceAsync.Util.getInstance().getUserProfile(callback);


        // Check login status using login service.
        LoginServiceAsync.Util.getInstance().login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
            public void onFailure(@NotNull final Throwable error) {
                GWT.log("AsyncCallback Failed: login(): " + error.getMessage());

                getView().setUserStatusFailed();
            }

            public void onSuccess(@Nullable final LoginInfo result) {
                //TODO: Throw an exception instead of returning null?
                if(result == null) {
                    getView().setUserStatusFailed();
                } else {
                    getView().setLoginInfo(result);
                }
            }
        });
    }
}
package com.murrayc.bigoquiz.client.application.userprofile;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.quiz.BigOQuizPresenter;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistoryClient;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfilePresenter extends BigOQuizPresenter<UserProfilePresenter.MyView, UserProfilePresenter.MyProxy>
        implements UserProfileUserEditUiHandlers {

    interface MyView extends ContentView, HasUiHandlers<UserProfileUserEditUiHandlers> {
        void setUserStatusFailed();

        void setUserRecentHistory(final UserHistoryOverall result);
    }

    @ProxyStandard
    @NameToken(NameTokens.USER_PROFILE)
    interface MyProxy extends ProxyPlace<UserProfilePresenter> {
    }

    @Inject
    UserProfilePresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        getView().setUiHandlers(this);

        // Check login status using login service.
        getView().setLoadingLabelVisible(true);

        getAndShowHistory();
    }

    private void getAndShowHistory() {
        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        final UserHistoryClient client = GWT.create(UserHistoryClient.class);

        @NotNull final MethodCallback<UserHistoryOverall> callback = new MethodCallback<UserHistoryOverall>() {
            @Override
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);

                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    Log.error("AsyncCallback Failed with IllegalArgumentException: getUserRecentHistory()", ex);
                    onFailureGeneric();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getUserRecentHistory()", ex);
                    onFailureGeneric();
                }
            }

            @Override
            public void onSuccess(final Method method, final UserHistoryOverall result) {
                getView().setLoadingLabelVisible(false);
                getView().setUserRecentHistory(result);
            }

            private void onFailureGeneric() {
                Log.fatal("debug: onFailureGeneric().");
                getView().setServerFailed();
            }
        };

        client.get(Window.Location.getHref(), callback);
    }
}
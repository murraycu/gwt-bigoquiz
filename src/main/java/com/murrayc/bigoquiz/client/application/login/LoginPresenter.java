package com.murrayc.bigoquiz.client.application.login;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.quiz.BigOQuizPresenter;
import com.murrayc.bigoquiz.client.application.userstatus.UserClient;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class LoginPresenter extends BigOQuizPresenter<LoginPresenter.MyView, LoginPresenter.MyProxy>
        implements LoginUserEditUiHandlers {

    interface MyView extends ContentView, HasUiHandlers<LoginUserEditUiHandlers> {
        void setLoginInfo(@NotNull final LoginInfo result);
        void setShowLogOutWhenAppropriate(boolean show);

        void setUserStatusFailed();
    }

    @ProxyStandard
    @NameToken(NameTokens.LOGIN)
    interface MyProxy extends ProxyPlace<LoginPresenter> {
    }

    @Inject
    LoginPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        final LoginPresenter.MyView theView = getView();
        theView.setUiHandlers(this);
        theView.setShowLogOutWhenAppropriate(false); //Default.

        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        final UserClient client = GWT.create(UserClient.class);

        // Check login status using login service.
        client.get(new MethodCallback<LoginInfo>() {
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.fatal("AsyncCallback Failed with IllegalArgumentException: login()", ex);
                    getView().setUserStatusFailed();
                } catch (final Throwable ex) {
                    Log.fatal("AsyncCallback Failed: login()", ex);
                    getView().setUserStatusFailed();
                }
            }

            public void onSuccess(final Method method, @Nullable final LoginInfo result) {
                Log.fatal("AsyncCallback succeeded: login()");

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
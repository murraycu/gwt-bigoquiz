package com.murrayc.bigoquiz.client.application.userstatus;


import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserStatusPresenter extends PresenterWidget<UserStatusPresenter.MyView>
        implements UserStatusUserEditUiHandlers {
    public interface MyView extends View, HasUiHandlers<UserStatusUserEditUiHandlers> {
        void setLoginInfo(@NotNull final LoginInfo result);
        void setShowLogOutWhenAppropriate(boolean show);

        void setUserStatusFailed();
    }

    @Inject
    UserStatusPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        final MyView theView = getView();
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
                    Log.error("AsyncCallback Failed with IllegalArgumentException: login()", ex);
                    getView().setUserStatusFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: login()", ex);
                    getView().setUserStatusFailed();
                }
            }

            public void onSuccess(final Method method, @Nullable final LoginInfo result) {
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

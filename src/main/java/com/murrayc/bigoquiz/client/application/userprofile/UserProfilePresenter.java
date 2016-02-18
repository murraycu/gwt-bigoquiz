package com.murrayc.bigoquiz.client.application.userprofile;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.DefaultUserHistoryRequestEvent;
import com.murrayc.bigoquiz.client.application.userhistoryrecent.UserHistoryRecentPresenter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfilePresenter extends Presenter<UserProfilePresenter.MyView, UserProfilePresenter.MyProxy>
        implements UserProfileUserEditUiHandlers {
    private final UserHistoryRecentPresenter userHistoryRecentPresenter;


    interface MyView extends View, HasUiHandlers<UserProfileUserEditUiHandlers> {
        void setUserStatusFailed();

        void setLoginInfo(@NotNull final LoginInfo result);
    }

    @ProxyStandard
    @NameToken(NameTokens.USER_PROFILE)
    interface MyProxy extends ProxyPlace<UserProfilePresenter> {
    }

    public static final SingleSlot SLOT_USER_HISTORY_RECENT = new SingleSlot();

    @Inject
    UserProfilePresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            UserHistoryRecentPresenter userHistoryRecentPresenter) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        this.userHistoryRecentPresenter = userHistoryRecentPresenter;

        getView().setUiHandlers(this);

        // Check login status using login service.
        LoginServiceAsync.Util.getInstance().login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
            public void onFailure(@NotNull final Throwable caught) {
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

    @Override
    protected void onBind() {
        super.onBind();

        setInSlot(SLOT_USER_HISTORY_RECENT, userHistoryRecentPresenter);
    }

    @Override
    public void onReset() {
        super.onReset();

        DefaultUserHistoryRequestEvent.fire(this);
    }

    @Override
    public void onResetSections() {
        QuizServiceAsync.Util.getInstance().resetSections(new AsyncCallback<Void>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: resetSections()", ex);
                    getView().setUserStatusFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: resetSections()", ex);
                    getView().setUserStatusFailed();
                }
            }

            @Override
            public void onSuccess(Void result) {
                tellUserHistoryPresenterAboutResetSections();
            }
        });
    }

    private void tellUserHistoryPresenterAboutResetSections() {
        UserProfileResetSectionsEvent.fire(this);
    }
}
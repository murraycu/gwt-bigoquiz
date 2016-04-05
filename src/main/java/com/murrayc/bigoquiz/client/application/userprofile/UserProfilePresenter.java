package com.murrayc.bigoquiz.client.application.userprofile;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.DefaultUserHistoryRequestEvent;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsPresenter;
import com.murrayc.bigoquiz.shared.QuizConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserProfilePresenter extends Presenter<UserProfilePresenter.MyView, UserProfilePresenter.MyProxy>
        implements UserProfileUserEditUiHandlers {
    private final UserHistorySectionsPresenter userHistorySectionsPresenter;


    interface MyView extends ContentView, HasUiHandlers<UserProfileUserEditUiHandlers> {
        void setUserStatusFailed();

        void setLoginInfo(@NotNull final LoginInfo result);
    }

    @ProxyStandard
    @NameToken(NameTokens.USER_PROFILE)
    interface MyProxy extends ProxyPlace<UserProfilePresenter> {
    }

    public static final SingleSlot<UserHistorySectionsPresenter> SLOT_USER_HISTORY_RECENT = new SingleSlot<>();

    @Inject
    UserProfilePresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            UserHistorySectionsPresenter userHistorySectionsPresenter) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        this.userHistorySectionsPresenter = userHistorySectionsPresenter;

        getView().setUiHandlers(this);

        // Check login status using login service.
        getView().setLoadingLabelVisible(true);
        LoginServiceAsync.Util.getInstance().login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
            public void onFailure(@NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);

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
                getView().setLoadingLabelVisible(false);

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

        setInSlot(SLOT_USER_HISTORY_RECENT, userHistorySectionsPresenter);
    }

    @Override
    public void onReset() {
        super.onReset();

        DefaultUserHistoryRequestEvent.fire(this);
    }

    //TODO: When we show other quizzes, this should be per-quiz on the quizzes page,
    //not on the user profile page.
    @Override
    public void onResetSections() {
        QuizServiceAsync.Util.getInstance().resetSections(getQuizId(), new AsyncCallback<Void>() {
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

    private String getQuizId() {
        return QuizConstants.DEFAULT_QUIZ_ID;
    }
}
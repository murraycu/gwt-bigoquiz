package com.murrayc.bigoquiz.client.application.userhistoryrecent;


import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentPresenter extends PresenterWidget<UserHistoryRecentPresenter.MyView>
        implements UserHistoryRecentUserEditUiHandlers {
    public interface MyView extends View, HasUiHandlers<UserHistoryRecentUserEditUiHandlers> {
    }

    @Inject
    UserHistoryRecentPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        getView().setUiHandlers(this);

        /* TODO:
        final AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: getUserProfile(): " + caught.getMessage());
                getView().setUserStatusFailed();
            }

            @Override
            public void onSuccess(final UserProfile result) {
                //TODO: Throw an exception instead of returning null?
                if(result == null) {
                    //getView().setUserStatusFailed();
                } else {
                    getView().setUserStatus(result);
                }
            }
        };
        */
    }
}
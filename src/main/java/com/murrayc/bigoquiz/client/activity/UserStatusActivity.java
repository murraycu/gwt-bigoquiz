package com.murrayc.bigoquiz.client.activity;

import com.google.gwt.activity.shared.AbstractActivity;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AcceptsOneWidget;
import com.murrayc.bigoquiz.client.ClientFactory;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginServiceAsync;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.ui.UserStatusView;
import com.murrayc.bigoquiz.client.ui.View;
import com.murrayc.bigoquiz.shared.db.UserProfile;

/**
 * Created by murrayc on 1/19/16.s
 */
public class UserStatusActivity extends AbstractActivity implements View.Presenter {
    private final ClientFactory clientFactory;
    private UserStatusView userStatusView;

    public UserStatusActivity(final ClientFactory clientFactory) {
        this.clientFactory = clientFactory;
    }

    @Override
    public void start(final AcceptsOneWidget panel, final EventBus eventBus) {
        userStatusView = clientFactory.getUserStatusView();
        userStatusView.setPresenter(this);
        panel.setWidget(userStatusView.asWidget());

        final AsyncCallback<UserProfile> callback = new AsyncCallback<UserProfile>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: login(): " + caught.getMessage());
            }

            @Override
            public void onSuccess(final UserProfile result) {
                userStatusView.setUserStatus(result);
            }
        };

        QuizServiceAsync.Util.getInstance().getUserProfile(callback);



        // Check login status using login service.
        LoginServiceAsync.Util.getInstance().login(GWT.getHostPageBaseURL(), new AsyncCallback<LoginInfo>() {
            public void onFailure(final Throwable error) {
                //TODO: Handle this: Log.error("login() failed.", error);
            }

            public void onSuccess(final LoginInfo result) {
                userStatusView.setLoginInfo(result);
            }
        });
    }

    @Override
    public void goTo(final Place place) {
        clientFactory.getPlaceController().goTo(place);
    }

}

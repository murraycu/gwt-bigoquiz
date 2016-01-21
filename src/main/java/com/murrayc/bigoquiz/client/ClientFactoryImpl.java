package com.murrayc.bigoquiz.client;

import com.google.gwt.event.shared.SimpleEventBus;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.murrayc.bigoquiz.client.place.QuestionPlace;
import com.murrayc.bigoquiz.client.ui.QuestionView;
import com.murrayc.bigoquiz.client.ui.QuestionViewImpl;
import com.murrayc.bigoquiz.client.ui.UserStatusView;
import com.murrayc.bigoquiz.client.ui.UserStatusViewImpl;

/**
 * Created by murrayc on 1/19/16.
 */
public class ClientFactoryImpl implements ClientFactory {
    private final EventBus eventBus = new SimpleEventBus();
    private final PlaceController placeController = new PlaceControllerExt(eventBus, new QuestionPlace("TODO"));

    private final UserStatusView userStatusView = new UserStatusViewImpl();
    private final QuestionView questionView = new QuestionViewImpl();

    @Override
    public EventBus getEventBus() {
        return eventBus;
    }

    @Override
    public PlaceController getPlaceController() {
        return placeController;
    }

    @Override
    public QuestionView getQuestionView() {
        return questionView;
    }

    @Override
    public UserStatusView getUserStatusView() {
        return userStatusView;
    }
}

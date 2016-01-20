package com.murrayc.bigoquiz.client;

import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;
import com.murrayc.bigoquiz.client.ui.QuestionView;
import com.murrayc.bigoquiz.client.ui.UserStatusView;

/**
 * Created by murrayc on 1/19/16.
 */
public interface ClientFactory {

    EventBus getEventBus();

    PlaceController getPlaceController();

    QuestionView getQuestionView();

    UserStatusView getUserStatusView();
}


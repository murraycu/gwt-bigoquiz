package com.murrayc.bigoquiz.client.ui;

import com.google.gwt.place.shared.Place;
import com.google.gwt.user.client.ui.IsWidget;

/**
 * Created by murrayc on 1/19/16.
 */
public interface View extends IsWidget {

    interface Presenter {
        void goTo(Place place);
    }

    void setPresenter(Presenter presenter);

    void clear();
}


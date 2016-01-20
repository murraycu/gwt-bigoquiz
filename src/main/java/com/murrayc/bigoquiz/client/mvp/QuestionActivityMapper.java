package com.murrayc.bigoquiz.client.mvp;

import com.google.gwt.activity.shared.Activity;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.place.shared.Place;
import com.murrayc.bigoquiz.client.ClientFactory;
import com.murrayc.bigoquiz.client.activity.QuestionActivity;
import com.murrayc.bigoquiz.client.place.QuestionPlace;

/**
 * Created by murrayc on 1/19/16.
 */
public class QuestionActivityMapper implements ActivityMapper {

    private final ClientFactory clientFactory;

    /**
     * ActivityMapper associates each Place with its corresponding {@link Activity}
     *
     * @param clientFactory
     *            Factory to be passed to activities
     */
    public QuestionActivityMapper(final ClientFactory clientFactory) {
        super();
        this.clientFactory = clientFactory;
    }

    /**
     * Map each Place to its corresponding Activity.
     */
    @Override
    public Activity getActivity(final Place place) {
        if (place instanceof QuestionPlace) {
            return new QuestionActivity((QuestionPlace) place, clientFactory);
        }

        return null;
    }

}


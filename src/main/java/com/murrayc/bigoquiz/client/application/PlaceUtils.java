package com.murrayc.bigoquiz.client.application;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.NameTokens;

/**
 * Created by murrayc on 1/24/16.
 */
public class PlaceUtils {
    public static PlaceRequest getPlaceRequestForQuestion(final String questionId) {
        return new PlaceRequest.Builder()
                        .nameToken(NameTokens.QUESTION)
                        .with(NameTokens.QUESTION_PARAM_QUESTION_ID, questionId)
                        .build();
    }
}

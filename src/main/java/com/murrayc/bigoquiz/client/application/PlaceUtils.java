package com.murrayc.bigoquiz.client.application;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.NameTokens;

/**
 * Created by murrayc on 1/24/16.
 */
public class PlaceUtils {
    public static PlaceRequest getPlaceRequestForQuestion(final String questionId, final String nextQuestionSectionId) {
        return new PlaceRequest.Builder()
                .nameToken(NameTokens.QUESTION)
                .with(NameTokens.QUESTION_PARAM_QUESTION_ID, questionId)
                .with(NameTokens.QUESTION_PARAM_NEXT_QUESTION_SECTION_ID, nextQuestionSectionId)
                .build();
    }

    /**
     * Get a PlaceRequest to show the next question from a particular section,
     * or from any section if no section is specified.
     *
     * @param nextQuestionSectionId
     * @return
     */
    public static PlaceRequest getPlaceRequestForSection(final String nextQuestionSectionId) {
        return new PlaceRequest.Builder()
                .nameToken(NameTokens.QUESTION)
                .with(NameTokens.QUESTION_PARAM_NEXT_QUESTION_SECTION_ID, nextQuestionSectionId)
                .build();
    }
}
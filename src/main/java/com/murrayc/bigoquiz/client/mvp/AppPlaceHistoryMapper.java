package com.murrayc.bigoquiz.client.mvp;

import com.google.gwt.place.shared.PlaceHistoryMapper;
import com.google.gwt.place.shared.WithTokenizers;
import com.murrayc.bigoquiz.client.place.QuestionPlace;

/**
 * PlaceHistoryMapper interface is used to attach all places which the PlaceHistoryHandler should be aware of. This is
 * done via the @WithTokenizers annotation or by extending PlaceHistoryMapperWithFactory and creating a separate
 * TokenizerFactory.
 * <p>
 * This code is mostly from AppPlaceHistoryMapper.java in the hellomvp GWT example:
 * <p>
 * https://code.google.com/webtoolkit/doc/latest/DevGuideMvpActivitiesAndPlaces.html
 */
@WithTokenizers({QuestionPlace.Tokenizer.class})
public interface AppPlaceHistoryMapper extends PlaceHistoryMapper {
}


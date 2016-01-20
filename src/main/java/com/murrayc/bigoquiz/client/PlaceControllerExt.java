package com.murrayc.bigoquiz.client;

/**
 * Created by murrayc on 1/19/16.
 */

import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceChangeEvent;
import com.google.gwt.place.shared.PlaceController;
import com.google.web.bindery.event.shared.EventBus;

/**
* @author Murray Cumming <murrayc@murrayc.com>
*
* See http://stackoverflow.com/questions/7672895/using-gwts-activities-and-places-can-i-navigate-to-a-previous-page-without-using?rq=1
*
*/
public class PlaceControllerExt extends PlaceController {

    private final Place defaultPlace;
    private Place previousPlace;
    private Place currentPlace;

    public PlaceControllerExt(EventBus eventBus, Place defaultPlace) {
        super(eventBus);
        this.defaultPlace = defaultPlace;
        eventBus.addHandler(PlaceChangeEvent.TYPE, new PlaceChangeEvent.Handler() {

            public void onPlaceChange(PlaceChangeEvent event) {

                previousPlace = currentPlace;
                currentPlace = event.getNewPlace();
            }
        });
    }

    public Place getDefaultPlace() {
        return defaultPlace;
    }

    /**
     * Navigate back to the previous Place. If there is no previous place then goto to default place. If there isn't one
     * of these then it'll go back to the default place as configured when the PlaceHistoryHandler was registered. This
     * is better than using History#back() as that can have the undesired effect of leaving the web app.
     */
    public void previous() {

        if (previousPlace != null) {
            goTo(previousPlace);
        } else {
            goTo(defaultPlace);
        }
    }
}


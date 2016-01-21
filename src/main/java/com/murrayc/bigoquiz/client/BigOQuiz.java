package com.murrayc.bigoquiz.client;

import com.google.gwt.activity.shared.ActivityManager;
import com.google.gwt.activity.shared.ActivityMapper;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.place.shared.Place;
import com.google.gwt.place.shared.PlaceController;
import com.google.gwt.place.shared.PlaceHistoryHandler;
import com.google.gwt.user.client.ui.*;
import com.google.web.bindery.event.shared.EventBus;
import com.murrayc.bigoquiz.client.mvp.AppPlaceHistoryMapper;
import com.murrayc.bigoquiz.client.mvp.QuestionActivityMapper;
import com.murrayc.bigoquiz.client.mvp.UserStatusActivityMapper;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class BigOQuiz implements EntryPoint {
    private final LayoutPanel rootLayoutPanel = RootLayoutPanel.get();
    private final FlowPanel layoutPanel = new FlowPanel();
    protected SimplePanel questionPanel = new SimplePanel();
    protected SimplePanel userStatusPanel = new SimplePanel();

    protected ClientFactory clientFactory;

    AcceptsOneWidget userStatusDisplay = new AcceptsOneWidget() {
        @Override
        public void setWidget(final IsWidget activityWidget) {
            final Widget widget = Widget.asWidgetOrNull(activityWidget);
            userStatusPanel.setVisible(widget != null);
            userStatusPanel.setWidget(widget);
        }
    };

    AcceptsOneWidget questionDisplay = new AcceptsOneWidget() {
        @Override
        public void setWidget(final IsWidget activityWidget) {
            final Widget widget = Widget.asWidgetOrNull(activityWidget);
            questionPanel.setVisible(widget != null);
            questionPanel.setWidget(widget);
        }
    };

    /**
     * This is the entry point method.
     */
    public void onModuleLoad() {
        rootLayoutPanel.add(layoutPanel);
        rootLayoutPanel.setWidgetVisible(layoutPanel, true);

        // add the display regions to the main layout panel
        layoutPanel.add(userStatusPanel);
        userStatusPanel.setVisible(true);
        layoutPanel.add(questionPanel);
        questionPanel.setVisible(true);

        // set some properties for the display regions
        // The 'overflow: visible' adds a horizontal scrollbar when the content is larger than the browser window.
        // TODO: It would be better to just have the regular browser scrollbars, but for some reason they
        // are not shown.
        rootLayoutPanel.getWidgetContainerElement(layoutPanel).getStyle().setOverflow(Style.Overflow.VISIBLE);

        // We might, in future, use different ClientFactory implementations to create different views
        // for different browser types (such as mobile), so we use GWT.create() to have deferred binding.
        // See http://code.google.com/webtoolkit/doc/latest/DevGuideMvpActivitiesAndPlaces.html
        // which describes how to do this via our .gwt.xml file.
        clientFactory = GWT.create(ClientFactory.class);
        final EventBus eventBus = clientFactory.getEventBus();
        final PlaceController placeController = clientFactory.getPlaceController();

        // Activity manager for the user status display region.
        final ActivityMapper userStatusActivityMapper = new UserStatusActivityMapper(clientFactory);
        final ActivityManager userStatusActivityManager = new ActivityManager(userStatusActivityMapper,
                eventBus);
        userStatusActivityManager.setDisplay(userStatusDisplay);

        // Activity manager for the question display region.
        final ActivityMapper questionActivityMapper = new QuestionActivityMapper(clientFactory);
        final ActivityManager questionActivityManager = new ActivityManager(questionActivityMapper, eventBus);
        questionActivityManager.setDisplay(questionDisplay);

        // Start PlaceHistoryHandler with our PlaceHistoryMapper.
        final AppPlaceHistoryMapper historyMapper = GWT.create(AppPlaceHistoryMapper.class);
        final PlaceHistoryHandler historyHandler = new PlaceHistoryHandler(historyMapper);

        Place defaultPlace = null;
        if (placeController instanceof PlaceControllerExt) {
            PlaceControllerExt ext = (PlaceControllerExt) placeController;
            defaultPlace = ext.getDefaultPlace();
        }
        historyHandler.register(placeController, eventBus, defaultPlace);

        // Goes to the place represented on the URL or the default place.
        historyHandler.handleCurrentHistory();
    }
}

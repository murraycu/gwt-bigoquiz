package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentView extends ViewWithUiHandlers<UserHistoryRecentUserEditUiHandlers>
        implements UserHistoryRecentPresenter.MyView {

    final VerticalPanel answersPanel = new VerticalPanel();
    private final PlaceManager placeManager;

    @Inject
    UserHistoryRecentView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        final Label labelTitle = new Label("Recent History");
        mainPanel.add(labelTitle);
        labelTitle.addStyleName("subsection-title");

        mainPanel.add(answersPanel);
        answersPanel.addStyleName("user-status-answers-panel");
        initWidget(mainPanel);
    }

    @Override
    public void setUserRecentHistory(final UserRecentHistory result) {
        answersPanel.clear();

        for (final UserAnswer userAnswer : result.getUserAnswers()) {
            final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(userAnswer.getQuestionId());
            final String url = placeManager.buildHistoryToken(placeRequest);
            final Hyperlink label = new Hyperlink(userAnswer.getQuestionTitle(), url);
            answersPanel.add(label);
        }
    }

    @Override
    public void setServerFailed() {
        //TODO: labelDebug.setText("Error: Connection to service failed.");

    }
}

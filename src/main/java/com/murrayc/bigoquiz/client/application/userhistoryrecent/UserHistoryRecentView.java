package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentView extends ViewWithUiHandlers<UserHistoryRecentUserEditUiHandlers>
        implements UserHistoryRecentPresenter.MyView {


    UserHistoryRecentView() {
        final FlowPanel statusPanel = new FlowPanel();
        statusPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        final Label labelTitle = new Label("Recent History");
        statusPanel.add(labelTitle);
        labelTitle.addStyleName("subsection-title");


        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-status-panel");
        mainPanel.add(labelTitle);
        initWidget(mainPanel);
    }
}

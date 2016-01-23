package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentView extends ViewWithUiHandlers<UserHistoryRecentUserEditUiHandlers>
        implements UserHistoryRecentPresenter.MyView {

    final VerticalPanel answersPanel = new VerticalPanel();

    UserHistoryRecentView() {
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
            final Anchor label = new Anchor(userAnswer.getQuestionTitle());

            //TODO: Do this the proper way and make it actually work:
            label.setHref("#" + NameTokens.HOME + "?question=" + userAnswer.getQuestionId());
            answersPanel.add(label);
        }
    }

    @Override
    public void setServerFailed() {
        //TODO: labelDebug.setText("Error: Connection to service failed.");

    }
}

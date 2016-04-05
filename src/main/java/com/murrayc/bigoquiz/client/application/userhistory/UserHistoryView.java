package com.murrayc.bigoquiz.client.application.userhistory;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.UserHistory;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.application.quiz.QuizPresenter;
import com.murrayc.bigoquiz.shared.Quiz;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryView extends ContentViewWithUIHandlers<UserHistoryUserEditUiHandlers>
        implements UserHistoryPresenter.MyView {
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Panel panelContent = new FlowPanel();
    private final PlaceManager placeManager;

    @Inject
    UserHistoryView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        setTitle(constants.sectionsTitle());

        //Sections sidebar:
        //We use a CSS media query to only show this on wider screens:
        @NotNull Panel sidebarPanelSections = new FlowPanel();
        parentPanel.add(sidebarPanelSections);
        sidebarPanelSections.addStyleName("sidebar-panel-sections");

        @NotNull SimplePanel userHistoryRecentPanel = new SimplePanel();
        sidebarPanelSections.add(userHistoryRecentPanel);
        bindSlot(UserHistoryPresenter.SLOT_USER_HISTORY_RECENT, userHistoryRecentPanel);
    }

    private void onResetSectionsButton(final String quizId) {
        //TODO: Do this in the presenter?
        Utils.showResetConfirmationDialog(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                getUiHandlers().onResetSections(quizId);
            }
        });
    }

    @Override
    public void setUserHistory(final UserHistory userHistory) {
        //TODO: Update..
    }

    @Override
    public void setServerFailed() {
        setErrorLabelVisible(true);
    }
}

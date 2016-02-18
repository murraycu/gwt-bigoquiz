package com.murrayc.bigoquiz.client.application.quizlist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.shared.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizListView extends ContentViewWithUIHandlers<QuizListUserEditUiHandlers>
        implements QuizListPresenter.MyView {
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Panel panelList = new FlowPanel();
    private final PlaceManager placeManager;

    @Inject
    QuizListView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        setTitle(constants.quizzesTitle());

        mainPanel.add(panelList);
        panelList.addStyleName("quiz-list-panel");
    }

    @Override
    public void setQuizList(@NotNull final List<Quiz.QuizDetails> quizList) {
        setErrorLabelVisible(false);

        panelList.clear();

        if (quizList == null) {
            Log.error("setQuizList(): quizList is null.");
            return;
        }

        for(final Quiz.QuizDetails details : quizList) {
            if (details == null) {
                Log.error("QuizListView: details is null.");
                continue;
            }

            @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuiz(details.getId());
            final String historyToken = placeManager.buildHistoryToken(placeRequest);
            @NotNull final Hyperlink link = new Hyperlink(details.getTitle(), historyToken);
            panelList.add(link);
        }
    }

    @Override
    public void setServerFailed() {
        setErrorLabelVisible(true);
    }
}

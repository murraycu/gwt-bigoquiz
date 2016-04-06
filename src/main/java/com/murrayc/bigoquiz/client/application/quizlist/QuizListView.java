package com.murrayc.bigoquiz.client.application.quizlist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
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

            final Panel rowPanel = new FlowPanel();
            rowPanel.addStyleName("quiz-list-row");
            rowPanel.addStyleName("clearfix");
            panelList.add(rowPanel);

            @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuiz(details.getId());
            final String historyToken = placeManager.buildHistoryToken(placeRequest);
            @NotNull final Hyperlink link = new InlineHyperlink(details.getTitle(), historyToken);
            rowPanel.add(link);


            final Button buttonPlay = new Button(constants.buttonAnswerQuestions());
            buttonPlay.addStyleName("button-answer-questions");
            buttonPlay.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    getUiHandlers().onAnswerQuestions(details.getId());
                }
            });
            rowPanel.add(buttonPlay);

            final Button buttonHistory = new Button(constants.buttonHistory());
            buttonHistory.addStyleName("button-history");
            buttonHistory.addClickHandler(new ClickHandler() {
                @Override
                public void onClick(final ClickEvent event) {
                    getUiHandlers().onHistory(details.getId());
                }
            });
            rowPanel.add(buttonHistory);
        }
    }

    @Override
    public void setServerFailed() {
        setErrorLabelVisible(true);
    }
}

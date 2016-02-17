package com.murrayc.bigoquiz.client.application.quizlist;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.shared.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizListView extends ViewImpl
        implements QuizListPresenter.MyView {
    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    private final Label labelError = Utils.createServerErrorLabel(constants);
    private final Panel panelList = new FlowPanel();
    private final PlaceManager placeManager;

    @Inject
    QuizListView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("content-panel");

        @NotNull final Label titleLabel = new Label(constants.quizzesTitle());
        titleLabel.addStyleName("page-title-label");
        mainPanel.add(titleLabel);

        mainPanel.add(labelError);

        mainPanel.add(panelList);
        panelList.addStyleName("quiz-list-panel");

        initWidget(mainPanel);
    }

    @Override
    public void setQuizList(@NotNull final List<Quiz.QuizDetails> quizList) {
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
            final String url = placeManager.buildHistoryToken(placeRequest);
            @NotNull final Hyperlink link = new Hyperlink(details.getTitle(), url);
            panelList.add(link);
        }
    }

    @Override
    public void setServerFailed() {
        labelError.setVisible(true);
    }
}

package com.murrayc.bigoquiz.client.application.home;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlowPanel;
import com.murrayc.bigoquiz.client.HtmlResources;
import com.murrayc.bigoquiz.client.application.ContentViewWithUIHandlers;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.shared.QuizConstants;

/**
 * Created by murrayc on 1/21/16.
 */
public class HomeView extends ContentViewWithUIHandlers<HomeUserEditUiHandlers>
        implements HomePresenter.MyView {

    HomeView() {
        //If we just add the HTML directly to mainPanel, we will lose its CSS id.
        final FlowPanel panel = new FlowPanel();
        Utils.addHtmlToPanel(panel, HtmlResources.INSTANCE.getHomeHtml());
        mainPanel.add(panel);

        final Button buttonPlay = new Button(constants.buttonAnswerQuestions());
        buttonPlay.addStyleName("home-button-answer-questions");
        buttonPlay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                getUiHandlers().onAnswerQuestions(QuizConstants.DEFAULT_QUIZ_ID);
            }
        });
        mainPanel.add(buttonPlay);
    }
}

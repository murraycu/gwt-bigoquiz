package com.murrayc.bigoquiz.client.application.home;

import com.google.gwt.dom.client.ParagraphElement;
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
        panel.addStyleName("home-panel");
        mainPanel.add(panel);

        //TODO: Find a cleaner way to have a button inbetween the paragraphs of text,
        //without splitting the HTML into 2 files.
        final Button buttonPlay = new Button(constants.buttonAnswerQuestions());
        buttonPlay.addStyleName("home-button-answer-questions");
        buttonPlay.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                getUiHandlers().onAnswerQuestions(QuizConstants.DEFAULT_QUIZ_ID);
            }
        });
        mainPanel.add(buttonPlay);

        final FlowPanel panel2 = new FlowPanel();
        Utils.addHtmlToPanel(panel2, HtmlResources.INSTANCE.getHomeEndHtml());
        panel2.addStyleName("home-panel");
        mainPanel.add(panel2);

    }
}

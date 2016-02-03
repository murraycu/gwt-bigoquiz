package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;

import java.util.Collection;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentView extends ViewWithUiHandlers<UserHistoryRecentUserEditUiHandlers>
        implements UserHistoryRecentPresenter.MyView {

    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    String nextQuestionSectionId;

    final FlowPanel detailsPanel = new FlowPanel();

    private final Label loginLabel = new Label(constants.pleaseSignIn());

    private final PlaceManager placeManager;

    private UserRecentHistory userRecentHistory;

    //When we skip building of the UI because its hidden (by CSS) anyway,
    //this lets us do that building if necessary later.
    private boolean buildUiPending = false;

    @Inject
    UserHistoryRecentView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        Utils.addHeaderToPanel(2, mainPanel, constants.sectionsTitle());

        //This is only visible when necessary:
        mainPanel.add(loginLabel);
        loginLabel.setVisible(false);

        mainPanel.add(detailsPanel);
        detailsPanel.addStyleName("user-status-answers-panel");
        initWidget(mainPanel);

        //Listen to window resizes, because that could trigger
        //this UI being visible again,
        //at which time we should build it:
        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent event) {
                if (buildUiPending) {
                    buildUi();
                }
            }
        });
    }

    @Override
    public void setUserRecentHistory(final UserRecentHistory userRecentHistory, final String nextQuestionSectionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;
        this.userRecentHistory = userRecentHistory;

        buildUi();
    }

    private void buildUi() {
        loginLabel.setVisible(userRecentHistory == null);

        detailsPanel.clear();

        //This would just mean that the user is not logged in.
        //TODO: Make this more explicit in the API.
        if (userRecentHistory == null) {
            return;
        }

        //TODO: Build it if detailsPanel becomes visible later,
        //for instance by changing orientation or resizing of the browser window.
        if (!Utils.widgetIsVisible(detailsPanel)) {
            //Don't bother building this if the whole sidebar is hidden,
            //for instance on devices with a smaller width,
            //via a CSS media query.
            buildUiPending = true;
            return;
        }

        buildUiPending = false;

        final QuizSections sections = userRecentHistory.getSections();
        if (sections == null) {
            return;
        }

        for (final String sectionId : sections.getSectionIds()) {
            final QuizSections.Section section = sections.getSection(sectionId);
            if (section == null) {
                continue;
            }


            final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForSection(sectionId);
            final String url = placeManager.buildHistoryToken(placeRequest);
            final Hyperlink titleLabel = new InlineHyperlink(section.title, url);
            //titleLabel.addStyleName("user-history-section-title-label");

            Utils.addHeaderToPanel(3, detailsPanel, titleLabel);

            final int count = section.questionsCount;

            final UserStats stats = userRecentHistory.getStats(sectionId);
            if (stats != null) {
                addStackedProgressBar(detailsPanel, stats.getCorrectOnce(), stats.getAnsweredOnce(), count);

                /* This doesn't seem interesting enough to take up the extra space with it:
                addParagraphWithText(detailsPanel,
                        messages.scoreMessage(stats.getAnswered(), stats.getCorrect()),
                        "label-score");
                */
            } else {
                GWT.log("buildUi(): UserStats is null.");
            }

            Utils.addHeaderToPanel(4, detailsPanel, constants.problemQuestions());
            //label.addStyleName("label-problem-questions");

            final Panel problemQuestionsPanel = new FlowPanel();
            detailsPanel.add(problemQuestionsPanel);
            problemQuestionsPanel.addStyleName("panel-problem-questions");

            final Collection<UserQuestionHistory> problemQuestions = stats.getQuestionHistories();
            if (problemQuestions == null || problemQuestions.isEmpty()) {
                Utils.addParagraphWithText(problemQuestionsPanel, constants.problemQuestionsNoneYet(),
                        "problem-answer-score");
            } else {
                for (final UserQuestionHistory problemQuestion : problemQuestions) {
                    final Panel paraScore = Utils.addParagraph(problemQuestionsPanel);

                    final String strScore = "-" + problemQuestion.getCountAnsweredWrong();
                    final Label labelScore = new InlineLabel(strScore);
                    labelScore.addStyleName("problem-answer-score");
                    paraScore.add(labelScore);

                    final Hyperlink link = createProblemQuestionHyperlink(problemQuestion, nextQuestionSectionId);
                    paraScore.add(link);
                }
            }


        }
    }

    private void addStackedProgressBar(final FlowPanel parentPanel, int correctOnce, int answeredOnce, int count) {
        final FlowPanel panelProgress = new FlowPanel();
        parentPanel.add(panelProgress);
        panelProgress.addStyleName("progress-bar");
        //panelProgress.addStyleName("clearfix");

        final String correctStr = messages.correctOnce(correctOnce);
        final String answeredStr = messages.answeredOnce(answeredOnce);
        final String countStr = messages.questionsCount(count);

        final Panel partCorrect = Utils.addParagraphWithText(panelProgress, correctStr, "progress-part-correct-once");
        final Panel partAnswered = Utils.addParagraphWithText(panelProgress, answeredStr, "progress-part-answered-once");
        final Panel partCount = Utils.addParagraphWithText(panelProgress, countStr, "progress-part-count");

        final double countDouble = (double)count;
        final double correctPercentage = (count == 0 ? 0 : (double)correctOnce / countDouble) * 100;
        final String correctWidthStr = NumberFormat.getFormat("#").format(correctPercentage) + "%";
        final double answeredPercentage = (count == 0 ? 0 : (double)answeredOnce / countDouble) * 100;
        final String answeredWidthStr = NumberFormat.getFormat("#").format(answeredPercentage) + "%";

        partCorrect.setWidth(correctWidthStr);
        partAnswered.setWidth(answeredWidthStr);
        partCount.setWidth("100%");
    }

    @Override
    public void addUserAnswer(final Question question, boolean answerIsCorrect) {
        if (userRecentHistory == null) {
            //The user is not logged in, so we don't show history.
            //TODO: See buildUi(), which makes the same assumption.
            return;
        }

        userRecentHistory.addUserAnswerAtStart(question, answerIsCorrect);

        //Re-generate the whole list in the UI:
        buildUi();
    }

    @Override
    public void setQuestionNextSectionId(final String nextQuestionSectionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;
        buildUi();
    }

    private Hyperlink createProblemQuestionHyperlink(final UserQuestionHistory problemQuestion, final String nextQuestionSectionId) {
        final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(problemQuestion.getQuestionId(), nextQuestionSectionId);
        final String url = placeManager.buildHistoryToken(placeRequest);
        final Hyperlink result = new InlineHyperlink(problemQuestion.getSubSectionTitle() + ": " + problemQuestion.getQuestionTitle(), url);
        result.addStyleName("problem-answer-hyperlink");
        return result;
    }

    @Override
    public void setServerFailed() {
        //TODO: labelDebug.setText("Error: Connection to service failed.");

    }
}

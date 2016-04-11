package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.ui.BigOQuizConstants;
import com.murrayc.bigoquiz.client.BigOQuizMessages;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.StringUtils;
import com.murrayc.bigoquiz.shared.db.UserQuestionHistory;
import com.murrayc.bigoquiz.shared.db.UserStats;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentView extends ViewWithUiHandlers<UserHistoryRecentUserEditUiHandlers>
        implements UserHistoryRecentPresenter.MyView {

    final Timer autoBuildUiTimer = new Timer() {
        @Override
        public void run() {
            if (buildUiPending) {
                buildUi();
            }
        }
    };

    // BigOQuizConstants.java is generated in the target/ directory,
    // from BigOQuizConstants.properties
    // by the gwt-maven-plugin's i18n (mvn:i18n) goal.
    private final BigOQuizConstants constants = GWT.create(BigOQuizConstants.class);
    private final BigOQuizMessages messages = GWT.create(BigOQuizMessages.class);

    String nextQuestionSectionId = null;
    boolean multipleChoice = true;

    final FlowPanel detailsPanel = new FlowPanel();

    private Panel loginParagraph = null;
    private final InlineHTML loginLabel = new InlineHTML();

    private final PlaceManager placeManager;

    private UserRecentHistory userRecentHistory = null;

    //When we skip building of the UI because its hidden (by CSS) anyway,
    //this lets us do that building if necessary later.
    private boolean buildUiPending = false;
    private final Label labelError = Utils.createServerErrorLabel(constants);
    private String quizId = null;

    @Inject
    UserHistoryRecentView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        Utils.addHeaderToPanel(2, mainPanel, constants.sectionsTitle());
        mainPanel.add(labelError);

        //This is only visible when necessary:
        loginParagraph = Utils.addParagraphWithChild(mainPanel, loginLabel);
        loginParagraph.setVisible(false);

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
    public void setUserRecentHistory(final String quizId, final UserRecentHistory userRecentHistory, final String nextQuestionSectionId, boolean multipleChoice) {
        this.quizId = quizId;
        this.nextQuestionSectionId = nextQuestionSectionId;
        this.userRecentHistory = userRecentHistory;
        this.multipleChoice = multipleChoice;

        buildUi();
    }

    private void buildUi() {
        //Some defaults:
        loginParagraph.setVisible(false);
        labelError.setVisible(false);

        detailsPanel.clear();

        if (userRecentHistory == null) {
            Log.error("buildUI(): userRecentHistory is null.");
            return;
        }

        if (!userRecentHistory.hasUser()) {
            final LoginInfo loginInfo = userRecentHistory.getLoginInfo();
            loginLabel.setHTML(messages.pleaseSignIn(loginInfo.getLoginUrl()));
            loginParagraph.setVisible(true);
        }

        //TODO: Build it if detailsPanel becomes visible later,
        //for instance by changing orientation or resizing of the browser window.
        if (!Utils.widgetIsVisible(detailsPanel)) {
            //Don't bother building this if the whole sidebar is hidden,
            //for instance on devices with a smaller width,
            //via a CSS media query.
            buildUiPending = true;

            //Try again later.
            //We listen for window resizes that could change the visibility,
            //by triggering our CSS media queries for page width.
            //
            //But this is an extra horrible workaround to deal with this view
            //not being visible sometimes until just after this buildUi() code has run.
            autoBuildUiTimer.schedule(2000);
            return;
        }

        buildUiPending = false;

        @Nullable final QuizSections sections = userRecentHistory.getSections();
        if (sections == null) {
            return;
        }

        @NotNull final List<QuizSections.Section> sectionItems = sections.getSectionsSorted();

        for (@Nullable final QuizSections.Section section : sectionItems) {
            if (section == null) {
                continue;
            }

            final String sectionId = section.id;
            if (StringUtils.isEmpty(sectionId)) {
                continue;
            }

            @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForSection(quizId, sectionId, multipleChoice);
            final String historyToken = placeManager.buildHistoryToken(placeRequest);
            @NotNull final Hyperlink titleLabel = new InlineHyperlink(section.title, historyToken);
            //titleLabel.addStyleName("user-history-section-title-label");

            Utils.addHeaderToPanel(3, detailsPanel, titleLabel);

            final int questionsCount = section.questionsCount;

            final UserStats stats = userRecentHistory.getStats(sectionId);
            if (stats != null) {
                addStackedProgressBar(detailsPanel, stats.getCorrectOnce(), stats.getAnsweredOnce(), questionsCount);

                /* This doesn't seem interesting enough to take up the extra space with it:
                addParagraphWithText(detailsPanel,
                        messages.scoreMessage(stats.getAnswered(), stats.getCorrect()),
                        "label-score");
                */
            } else {
                Log.error("buildUi(): UserStats is null.");
                return;
            }

            Utils.addHeaderToPanel(4, detailsPanel, constants.problemQuestions());
            //label.addStyleName("label-problem-questions");

            @NotNull final Panel problemQuestionsPanel = new FlowPanel();
            detailsPanel.add(problemQuestionsPanel);
            problemQuestionsPanel.addStyleName("panel-problem-questions");

            @NotNull final List<UserQuestionHistory> problemQuestions = stats.getTopProblemQuestionHistories();

            int count = 0;
            if (problemQuestions != null) {
                for (@NotNull final UserQuestionHistory problemQuestion : problemQuestions) {
                    if (problemQuestion.getCountAnsweredWrong() <= 0) {
                        //It's not really a problem question.
                        continue;
                    }

                    //This shouldn't be necessary, because the server should not return to many,
                    //but let's be sure:
                    if (count >= UserStats.MAX_PROBLEM_QUESTIONS) {
                        break;
                    }

                    @NotNull final Panel paraScore = addParaScore(problemQuestionsPanel);

                    @NotNull final String strScore = "-" + problemQuestion.getCountAnsweredWrong();
                    @NotNull final Label labelScore = new InlineLabel(strScore);
                    labelScore.addStyleName("problem-answer-score");
                    paraScore.add(labelScore);

                    @NotNull final Hyperlink link = createProblemQuestionHyperlink(problemQuestion, nextQuestionSectionId, multipleChoice);
                    paraScore.add(link);

                    count += 1;
                }

                final int fullCount = stats.getProblemQuestionHistoriesCount();
                Log.fatal("debug: fullCount=" + fullCount);
                if (fullCount > UserStats.MAX_PROBLEM_QUESTIONS) {
                    final int extras = fullCount - UserStats.MAX_PROBLEM_QUESTIONS;
                    Log.fatal("debug: extras=" + extras);

                    if (extras > 0) {
                        Utils.addParagraphWithText(problemQuestionsPanel, messages.moreProblemQuestions(extras),
                                "problem-questions-more-questions");
                    }
                }
            }

            if (count == 0) {
                //We put it in a parent paragraph, like the real problem questions,
                //so it can take up the full width.
                @NotNull final Panel paraScore = addParaScore(problemQuestionsPanel);
                Utils.addParagraphWithText(paraScore, constants.problemQuestionsNoneYet(),
                        "problem-answer-score");
            }


        }
    }

    @NotNull
    private static Panel addParaScore(Panel problemQuestionsPanel) {
        @NotNull final Panel paraScore = Utils.addParagraph(problemQuestionsPanel,
                "problem-answer");
        paraScore.addStyleName("clearfix");
        return paraScore;
    }

    private void addStackedProgressBar(@NotNull final FlowPanel parentPanel, int correctOnce, int answeredOnce, int count) {
        @NotNull final FlowPanel panelProgress = new FlowPanel();
        parentPanel.add(panelProgress);
        panelProgress.addStyleName("progress-bar");
        //panelProgress.addStyleName("clearfix");

        @NotNull final String correctStr = messages.correctOnce(correctOnce);
        @NotNull final String answeredStr = messages.answeredOnce(answeredOnce);
        @NotNull final String countStr = messages.questionsCount(count);

        @NotNull final Panel partCorrect = Utils.addParagraphWithText(panelProgress, correctStr, "progress-part-correct-once");
        @NotNull final Panel partAnswered = Utils.addParagraphWithText(panelProgress, answeredStr, "progress-part-answered-once");
        @NotNull final Panel partCount = Utils.addParagraphWithText(panelProgress, countStr, "progress-part-count");

        final double countDouble = (double)count;
        final double correctPercentage = (count == 0 ? 0 : (double)correctOnce / countDouble) * 100;
        @NotNull final String correctWidthStr = NumberFormat.getFormat("#").format(correctPercentage) + "%";
        final double answeredPercentage = (count == 0 ? 0 : (double)answeredOnce / countDouble) * 100;
        @NotNull final String answeredWidthStr = NumberFormat.getFormat("#").format(answeredPercentage) + "%";

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

        userRecentHistory.addUserAnswerAtStart(quizId, question, answerIsCorrect);

        //Re-generate the whole list in the UI:
        buildUi();
    }

    @Override
    public void setQuestionContext(final String nextQuestionSectionId, boolean multipleChoice) {
        this.nextQuestionSectionId = nextQuestionSectionId;
        this.multipleChoice = multipleChoice;
        buildUi();
    }

    @NotNull
    private Hyperlink createProblemQuestionHyperlink(@NotNull final UserQuestionHistory problemQuestion, final String nextQuestionSectionId, final boolean multipleChoice) {
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(getQuizId(), problemQuestion.getQuestionId(), nextQuestionSectionId, multipleChoice);
        final String historyToken = placeManager.buildHistoryToken(placeRequest);
        final String title = problemQuestion.getSubSectionTitle() + ": " + problemQuestion.getQuestionTitle();
        @NotNull final Hyperlink result = new InlineHyperlink(title, historyToken);
        result.addStyleName("problem-answer-hyperlink");
        return result;
    }

    private String getQuizId() {
        return quizId;
    }

    @Override
    public void setServerFailed() {
        labelError.setVisible(true);
    }
}

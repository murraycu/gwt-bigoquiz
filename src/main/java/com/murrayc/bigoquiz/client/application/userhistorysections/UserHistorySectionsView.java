package com.murrayc.bigoquiz.client.application.userhistorysections;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.Timer;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.UserHistorySections;
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
public class UserHistorySectionsView extends ViewWithUiHandlers<UserHistorySectionsUserEditUiHandlers>
        implements UserHistorySectionsPresenter.MyView {

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

    final FlowPanel detailsPanel = new FlowPanel();

    private Panel loginParagraph = null;
    private final InlineHTML loginLabel = new InlineHTML();

    private final PlaceManager placeManager;

    private UserHistorySections userHistorySections = null;

    //When we skip building of the UI because its hidden (by CSS) anyway,
    //this lets us do that building if necessary later.
    private boolean buildUiPending = false;
    private final Label labelError = Utils.createServerErrorLabel(constants);
    private String quizId = null;

    @Inject
    UserHistorySectionsView(PlaceManager placeManager) {
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
        Window.addResizeHandler(event -> {
            if (buildUiPending) {
                buildUi();
            }
        });
    }

    @Override
    public void setUserRecentHistory(final String quizId, final UserHistorySections userHistorySections, final String nextQuestionSectionId) {
        this.quizId = quizId;
        this.nextQuestionSectionId = nextQuestionSectionId;
        this.userHistorySections = userHistorySections;

        buildUi();
    }

    private void buildUi() {
        //Some defaults:
        loginParagraph.setVisible(false);
        labelError.setVisible(false);

        detailsPanel.clear();

        //This is allowed, to clear the panel if the quizId is invalid.
        if (userHistorySections == null) {
            return;
        }

        if (!userHistorySections.hasUser()) {
            final LoginInfo loginInfo = userHistorySections.getLoginInfo();
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

        @Nullable final QuizSections sections = userHistorySections.getSections();
        if (sections == null) {
            return;
        }

        @NotNull final List<QuizSections.Section> sectionItems = sections.getSections();

        for (@Nullable final QuizSections.Section section : sectionItems) {
            if (section == null) {
                continue;
            }

            final String sectionId = section.getId();
            if (StringUtils.isEmpty(sectionId)) {
                continue;
            }

            @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForSection(quizId, sectionId);
            final String historyToken = placeManager.buildHistoryToken(placeRequest);
            @NotNull final Hyperlink titleLabel = new InlineHyperlink(section.getTitle(), historyToken);
            //titleLabel.addStyleName("user-history-section-title-label");

            Utils.addHeaderToPanel(3, detailsPanel, titleLabel);

            final UserStats stats = userHistorySections.getStats(sectionId);
            if (stats != null) {
                // We limit answeredOnce and correctOnce because these can be bigger than
                // the actual available count of questions, if we change the question DIs after they've been answered,
                // but this would look strange to people.
                // TODO: Actually forget now-invalid answered questions.
                final int questionsCount = section.getQuestionsCount()  ;
                final int answeredOnce = Math.min(stats.getAnsweredOnce(), questionsCount);
                final int correctOnce = Math.min(stats.getCorrectOnce(), questionsCount);
                Utils.addStackedProgressBar(detailsPanel, correctOnce, answeredOnce, questionsCount, messages);

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

                    @NotNull final Panel paraScore = Utils.addParaScore(problemQuestionsPanel);

                    @NotNull final String strScore = "-" + problemQuestion.getCountAnsweredWrong();
                    @NotNull final Label labelScore = new InlineLabel(strScore);
                    labelScore.addStyleName("problem-answer-score");
                    paraScore.add(labelScore);

                    @NotNull final Hyperlink link = createProblemQuestionHyperlink(problemQuestion, nextQuestionSectionId);
                    paraScore.add(link);

                    count += 1;
                }

                final int fullCount = stats.getProblemQuestionHistoriesCount();
                if (fullCount > UserStats.MAX_PROBLEM_QUESTIONS) {
                    final int extras = fullCount - UserStats.MAX_PROBLEM_QUESTIONS;

                    if (extras > 0) {
                        Utils.addParagraphWithText(problemQuestionsPanel, messages.moreProblemQuestions(extras),
                                "problem-questions-more-questions");
                    }
                }
            }

            if (count == 0) {
                //We put it in a parent paragraph, like the real problem questions,
                //so it can take up the full width.
                @NotNull final Panel paraScore = Utils.addParaScore(problemQuestionsPanel);
                Utils.addParagraphWithText(paraScore, constants.problemQuestionsNoneYet(),
                        "problem-answer-score");
            }


        }
    }

    @Override
    public void addUserAnswer(final Question question, boolean answerIsCorrect) {
        if (userHistorySections == null) {
            //The user is not logged in, so we don't show history.
            //TODO: See buildUi(), which makes the same assumption.
            return;
        }

        userHistorySections.addUserAnswerAtStart(quizId, question, answerIsCorrect);

        //Re-generate the whole list in the UI:
        buildUi();
    }

    @Override
    public void setQuestionContext(final String nextQuestionSectionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;
        buildUi();
    }

    @NotNull
    private Hyperlink createProblemQuestionHyperlink(@NotNull final UserQuestionHistory problemQuestion, final String nextQuestionSectionId) {
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(getQuizId(), problemQuestion.getQuestionId(), nextQuestionSectionId);
        final String historyToken = placeManager.buildHistoryToken(placeRequest);
        final String subSectionTitle = problemQuestion.getSubSectionTitle();
        final Question.Text title = problemQuestion.getQuestionTitle();
        String titleText = title.text;
        if (!StringUtils.isEmpty(subSectionTitle)) {
            titleText = subSectionTitle + ": " + titleText;
        }

        final Hyperlink result;
        if (title.isHtml) {
            //TODO: Use modified SimpleHtmlSanitizer?
            result = new InlineHyperlink(SafeHtmlUtils.fromTrustedString(titleText), historyToken);
        } else {
            result = new InlineHyperlink(titleText, historyToken);

        }
        result.addStyleName("problem-answer-hyperlink");
        return result;
    }

    private String getQuizId() {
        return quizId;
    }

    @Override
    public void setServerFailed() {
        labelError.setText(constants.errorNoServer());
        labelError.setVisible(true);
    }

    @Override
    public void setServerFailedUnknownQuiz() {
        labelError.setText(constants.errorUnknownQuiz());
        labelError.setVisible(true);
    }
}

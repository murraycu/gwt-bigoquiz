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
    private Label labelError = new InlineLabel(constants.errorNoServer());

    @Inject
    UserHistoryRecentView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        @NotNull final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        Utils.addHeaderToPanel(2, mainPanel, constants.sectionsTitle());

        labelError.addStyleName("server-error-label");
        labelError.setVisible(false);
        mainPanel.add(labelError);

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
        labelError.setVisible(false);

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

        @Nullable final QuizSections sections = userRecentHistory.getSections();
        if (sections == null) {
            return;
        }

        @NotNull final List<QuizSections.Section> sectionItems = new ArrayList<>(sections.getSections());

        //Do the sorting here on the client-side,
        //because the titles could (one day) be localized,
        //and the sorting would need to depend on the user's locale too.
        Collections.sort(sectionItems,
                new Comparator<QuizSections.Section>() {
                    @Override
                    public int compare(@Nullable final QuizSections.Section o1, @Nullable final QuizSections.Section o2) {
                        if ((o1 == null) && (o2 == null)) {
                            return 0;
                        } else if (o1 == null) {
                            return -1;
                        }

                        if ((o1.title == null) && (o2.title == null)) {
                            return 0;
                        } else if (o1.title == null) {
                            return -1;
                        }

                        return o1.title.compareTo(o2.title);
                    }
                });

        for (@Nullable final QuizSections.Section section : sectionItems) {
            if (section == null) {
                continue;
            }

            final String sectionId = section.id;
            if (StringUtils.isEmpty(sectionId)) {
                continue;
            }

            @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForSection(sectionId);
            final String url = placeManager.buildHistoryToken(placeRequest);
            @NotNull final Hyperlink titleLabel = new InlineHyperlink(section.title, url);
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
                GWT.log("buildUi(): UserStats is null.");
            }

            Utils.addHeaderToPanel(4, detailsPanel, constants.problemQuestions());
            //label.addStyleName("label-problem-questions");

            @NotNull final Panel problemQuestionsPanel = new FlowPanel();
            detailsPanel.add(problemQuestionsPanel);
            problemQuestionsPanel.addStyleName("panel-problem-questions");

            @NotNull final Collection<UserQuestionHistory> problemQuestions = stats.getQuestionHistories();
            if (problemQuestions == null || problemQuestions.isEmpty()) {
                Utils.addParagraphWithText(problemQuestionsPanel, constants.problemQuestionsNoneYet(),
                        "problem-answer-score");
            } else {
                int count = 0;
                int extras = 0;
                final int MAX = 5;
                for (@NotNull final UserQuestionHistory problemQuestion : problemQuestions) {
                    if (count >= MAX) {
                        extras += 1;
                        continue;
                    }

                    if (problemQuestion.getCountAnsweredWrong() <= 0) {
                        //It's not really a problem question.
                        continue;
                    }

                    @NotNull final Panel paraScore = Utils.addParagraph(problemQuestionsPanel);

                    @NotNull final String strScore = "-" + problemQuestion.getCountAnsweredWrong();
                    @NotNull final Label labelScore = new InlineLabel(strScore);
                    labelScore.addStyleName("problem-answer-score");
                    paraScore.add(labelScore);

                    @NotNull final Hyperlink link = createProblemQuestionHyperlink(problemQuestion, nextQuestionSectionId);
                    paraScore.add(link);

                    count += 1;
                }

                if (extras > 0) {
                    Utils.addParagraphWithText(problemQuestionsPanel, messages.moreProblemQuestions(extras),
                            "problem-questions-more-questions");
                }
            }


        }
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

        userRecentHistory.addUserAnswerAtStart(question, answerIsCorrect);

        //Re-generate the whole list in the UI:
        buildUi();
    }

    @Override
    public void setQuestionNextSectionId(final String nextQuestionSectionId) {
        this.nextQuestionSectionId = nextQuestionSectionId;
        buildUi();
    }

    @NotNull
    private Hyperlink createProblemQuestionHyperlink(@NotNull final UserQuestionHistory problemQuestion, final String nextQuestionSectionId) {
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(problemQuestion.getQuestionId(), nextQuestionSectionId);
        final String url = placeManager.buildHistoryToken(placeRequest);
        @NotNull final Hyperlink result = new InlineHyperlink(problemQuestion.getSubSectionTitle() + ": " + problemQuestion.getQuestionTitle(), url);
        result.addStyleName("problem-answer-hyperlink");
        return result;
    }

    @Override
    public void setServerFailed() {
        labelError.setVisible(true);
    }
}

package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.HeadingElement;
import com.google.gwt.user.client.DOM;
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
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserAnswer;
import com.murrayc.bigoquiz.shared.db.UserProblemQuestion;
import com.murrayc.bigoquiz.shared.db.UserStats;

import java.util.List;

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

    final FlowPanel detailsPanel = new FlowPanel();

    private final Label loginLabel = new Label(constants.pleaseSignIn());

    private final PlaceManager placeManager;

    private UserRecentHistory userRecentHistory;

    @Inject
    UserHistoryRecentView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        Utils.addHeaderToPanel(2, mainPanel, constants.recentHistoryTitle());

        //This is only visible when necessary:
        mainPanel.add(loginLabel);
        loginLabel.setVisible(false);

        mainPanel.add(detailsPanel);
        detailsPanel.addStyleName("user-status-answers-panel");
        initWidget(mainPanel);
    }

    @Override
    public void setUserRecentHistory(final UserRecentHistory userRecentHistory) {
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

        final QuizSections sections = userRecentHistory.getSections();
        if (sections == null) {
            return;
        }

        for (final String sectionId : sections.getSectionIds()) {
            final HeadingElement h = Utils.addHeaderToPanel(3, detailsPanel, "");

            final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForSection(sectionId);
            final String url = placeManager.buildHistoryToken(placeRequest);
            final Hyperlink titleLabel = new InlineHyperlink(sections.getSectionTitle(sectionId), url);
            //titleLabel.addStyleName("user-history-section-title-label");

            DOM.insertChild(h, titleLabel.getElement(), 0);

            final UserStats stats = userRecentHistory.getStats(sectionId);
            if (stats != null) {
                final String strStats = messages.scoreMessage(stats.getAnswered(), stats.getCorrect());
                final Label labelStats = new Label(strStats);
                detailsPanel.add(labelStats);
                labelStats.addStyleName("label-stats");
            } else {
                GWT.log("buildUi(): UserStats is null.");
            }

            Utils.addHeaderToPanel(4, detailsPanel, constants.problemQuestions());
            //label.addStyleName("label-problem-questions");

            final Panel problemQuestionsPanel = new FlowPanel();
            detailsPanel.add(problemQuestionsPanel);
            problemQuestionsPanel.addStyleName("panel-problem-questions");

            final List<UserProblemQuestion> problemQuestions = userRecentHistory.getProblemQuestions(sectionId);
            if (problemQuestions == null || problemQuestions.isEmpty()) {
                final Panel p = Utils.addParagraph(problemQuestionsPanel);
                final Label labelScore = new InlineLabel(constants.problemQuestionsNoneYet());
                labelScore.addStyleName("problem-answer-score");
                p.add(labelScore);
            } else {
                for (final UserProblemQuestion problemQuestion : problemQuestions) {
                    final Panel p = Utils.addParagraph(problemQuestionsPanel);

                    final String strScore = "-" + problemQuestion.getCountAnsweredWrong();
                    final Label labelScore = new InlineLabel(strScore);
                    labelScore.addStyleName("problem-answer-score");
                    p.add(labelScore);

                    final Hyperlink link = createProblemQuestionrHyperlink(problemQuestion);
                    p.add(link);
                }
            }


        }
    }

    @Override
    public void addUserAnswer(final UserAnswer userAnswer) {
        if (userRecentHistory == null) {
            //The user is not logged in, so we don't show history.
            //TODO: See buildUi(), which makes the same assumption.
            return;
        }

        userRecentHistory.addUserAnswerAtStart(userAnswer);

        //Re-generate the whole list in the UI:
        buildUi();
    }

    private Hyperlink createUserAnswerHyperlink(final UserAnswer userAnswer) {
        //TODO: This will take the user to that question,
        //and keep any subsequent questions to that question's section,
        //by specifying the nextSectionQuestionId to getPlaceRequestForQuestion().
        //Alternatively, we could specify no section (meaning it would use questions from all sections).
        //Both alternatives lose whatever the user had set before clicking this link.
        final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(userAnswer.getQuestionId(), userAnswer.getSectionId());
        final String url = placeManager.buildHistoryToken(placeRequest);
        final Hyperlink result = new InlineHyperlink(userAnswer.getSubSectionTitle() + ": " + userAnswer.getQuestionTitle(), url);
        result.addStyleName("user-answer-hyperlink");
        return result;
    }

    private Hyperlink createProblemQuestionrHyperlink(final UserProblemQuestion problemQuestion) {
        //TODO: This will take the user to that question,
        //and keep any subsequent questions to that question's section,
        //by specifying the nextSectionQuestionId to getPlaceRequestForQuestion().
        //Alternatively, we could specify no section (meaning it would use questions from all sections).
        //Both alternatives lose whatever the user had set before clicking this link.
        final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(problemQuestion.getQuestionId(), problemQuestion.getSectionId());
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

package com.murrayc.bigoquiz.client.application.userhistoryrecent;

import com.google.gwt.user.client.ui.*;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewWithUiHandlers;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.shared.QuizSections;
import com.murrayc.bigoquiz.shared.db.UserAnswer;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentView extends ViewWithUiHandlers<UserHistoryRecentUserEditUiHandlers>
        implements UserHistoryRecentPresenter.MyView {

    final FlowPanel answersPanel = new FlowPanel();
    private final PlaceManager placeManager;

    private UserRecentHistory userRecentHistory;

    @Inject
    UserHistoryRecentView(PlaceManager placeManager) {
        this.placeManager = placeManager;

        final FlowPanel mainPanel = new FlowPanel();
        mainPanel.addStyleName("user-history-recent-panel");
        //box.getElement().setAttribute("id", "titlebox");

        final Label labelTitle = new Label("Recent History");
        mainPanel.add(labelTitle);
        labelTitle.addStyleName("subsection-title");

        mainPanel.add(answersPanel);
        answersPanel.addStyleName("user-status-answers-panel");
        initWidget(mainPanel);
    }

    @Override
    public void setUserRecentHistory(final UserRecentHistory userRecentHistory) {
        this.userRecentHistory = userRecentHistory;

        answersPanel.clear();

        final QuizSections sections = userRecentHistory.getSections();
        if (sections == null) {
            return;
        }

        for (final String sectionId : sections.getSectionIds()) {
            final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForSection(sectionId);
            final String url = placeManager.buildHistoryToken(placeRequest);
            final Hyperlink titleLabel = new Hyperlink(sections.getSectionTitle(sectionId), url);
            answersPanel.add(titleLabel);
            titleLabel.addStyleName("section-title-label");

            final Panel panel = new FlowPanel();
            answersPanel.add(panel);
            panel.addStyleName("panel-user-answers");
            for (final UserAnswer userAnswer : userRecentHistory.getUserAnswers(sectionId)) {
                final Hyperlink link = createUserAnswerHyperlink(userAnswer);
                panel.add(link);
            }
        }
    }

    @Override
    public void addUserAnswer(final UserAnswer userAnswer) {
        userRecentHistory.addUserAnswerAtStart(userAnswer);

        //Re-generate the whole list in the UI:
        setUserRecentHistory(userRecentHistory);
    }

    private Hyperlink createUserAnswerHyperlink(final UserAnswer userAnswer) {
        //TODO: This will take the user to that question,
        //and keep any subsequent questions to that question's section,
        //by specifying the nextSectionQuestionId to getPlaceRequestForQuestion().
        //Alternatively, we could specify no section (meaning it would use questions from all sections).
        //Both alternatives lose whatever the user had set before clicking this link.
        final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuestion(userAnswer.getQuestionId(), userAnswer.getSectionId());
        final String url = placeManager.buildHistoryToken(placeRequest);
        final Hyperlink result = new Hyperlink(userAnswer.getQuestionTitle(), url);
        result.addStyleName("user-answer-hyperlink");
        return result;
    }

    @Override
    public void setServerFailed() {
        //TODO: labelDebug.setText("Error: Connection to service failed.");

    }
}

package com.murrayc.bigoquiz.client.application.userhistoryrecent;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.client.application.question.QuestionNextQuestionSetionIdEvent;
import com.murrayc.bigoquiz.client.application.question.QuestionUserAnswerAddedEvent;
import com.murrayc.bigoquiz.client.application.userprofile.UserProfileResetSectionsEvent;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.StringUtils;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentPresenter extends PresenterWidget<UserHistoryRecentPresenter.MyView>
        implements UserHistoryRecentUserEditUiHandlers,
        QuestionUserAnswerAddedEvent.QuestionUserAnswerAddedEventHandler,
        QuestionNextQuestionSetionIdEvent.QuestionUserAnswerAddedEventHandler,
        UserProfileResetSectionsEvent.UserProfileResetSectionsEventHandler {

    private String nextQuestionSectionId;

    public interface MyView extends View, HasUiHandlers<UserHistoryRecentUserEditUiHandlers> {
        /** Set a whole set of history.
         */
        void setUserRecentHistory(final UserRecentHistory result, final String nextQuestionSectionId);

        /** Add a single item of history.
         * For instance, to avoid retrieving the whole history from the server,
         * if the new item is know already.
         */
        void addUserAnswer(final Question question, boolean answerIsCorrect);

        void setServerFailed();

        void setQuestionNextSectionId(final String nextQuestionSectionId);
    }

    @Inject
    UserHistoryRecentPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        getView().setUiHandlers(this);

        addRegisteredHandler(QuestionUserAnswerAddedEvent.TYPE, this);
        addRegisteredHandler(QuestionNextQuestionSetionIdEvent.TYPE, this);
        addRegisteredHandler(UserProfileResetSectionsEvent.TYPE, this);

        //TODO: If QUESTION_PARAM_NEXT_QUESTION_SECTION_ID was specified in the URL,
        //then QuestionPresenter will cause the UI to be rebuilt again,
        //making this first build of the UI (without a nextQuestionSectionId) a
        //waste of effort.
        getAndShowHistory();
    }

    @ProxyEvent
    @Override
    public void onQuestionUserAnswerAdded(final QuestionUserAnswerAddedEvent event) {
        getView().addUserAnswer(event.getQuestion(), event.getAnswerIsCorrect());
    }

    @ProxyEvent
    @Override
    public void onQuestionNextSectionId(final QuestionNextQuestionSetionIdEvent event) {
        final String nextQuestionSectionId = event.getNextQuestionSectionId();
        if (StringUtils.equals(this.nextQuestionSectionId, nextQuestionSectionId)) {
            //Do nothing.
            return;
        }

        this.nextQuestionSectionId = nextQuestionSectionId;

        getView().setQuestionNextSectionId(nextQuestionSectionId);
    }

    @ProxyEvent
    @Override
    public void onUserProfileResetSections(final UserProfileResetSectionsEvent event) {
        //Completely refresh the data from the server:
        getAndShowHistory();
    }

    private void getAndShowHistory() {
        final AsyncCallback<UserRecentHistory> callback = new AsyncCallback<UserRecentHistory>() {
            @Override
            public void onFailure(final Throwable caught) {
                // TODO: create a way to notify users of asynchronous callback failures
                GWT.log("AsyncCallback Failed: getUserRecentHistory(): " + caught.getMessage());
                getView().setServerFailed();
            }

            @Override
            public void onSuccess(final UserRecentHistory result) {
                getView().setUserRecentHistory(result, nextQuestionSectionId);
            }
        };

        QuizServiceAsync.Util.getInstance().getUserRecentHistory(callback);
    }

}
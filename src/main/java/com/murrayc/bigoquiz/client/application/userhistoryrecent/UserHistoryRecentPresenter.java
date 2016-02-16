package com.murrayc.bigoquiz.client.application.userhistoryrecent;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.UserRecentHistory;
import com.murrayc.bigoquiz.client.application.question.QuestionNextQuestionSectionIdEvent;
import com.murrayc.bigoquiz.client.application.question.QuestionUserAnswerAddedEvent;
import com.murrayc.bigoquiz.client.application.userprofile.UserProfileResetSectionsEvent;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryRecentPresenter extends PresenterWidget<UserHistoryRecentPresenter.MyView>
        implements UserHistoryRecentUserEditUiHandlers,
        QuestionUserAnswerAddedEvent.QuestionUserAnswerAddedEventHandler,
        QuestionNextQuestionSectionIdEvent.QuestionUserAnswerAddedEventHandler,
        UserProfileResetSectionsEvent.UserProfileResetSectionsEventHandler {

    private String nextQuestionSectionId;
    private boolean userIsLoggedIn = false;

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
        addRegisteredHandler(QuestionNextQuestionSectionIdEvent.TYPE, this);
        addRegisteredHandler(UserProfileResetSectionsEvent.TYPE, this);

        //TODO: If QUESTION_PARAM_NEXT_QUESTION_SECTION_ID was specified in the URL,
        //then QuestionPresenter will cause the UI to be rebuilt again,
        //making this first build of the UI (without a nextQuestionSectionId) a
        //waste of effort.
        getAndShowHistory();
    }

    @ProxyEvent
    @Override
    public void onQuestionUserAnswerAdded(@NotNull final QuestionUserAnswerAddedEvent event) {
        //Only keep track of the answers if we are logged in.
        //Otherwise users might be confused when they lose their progress after a refresh or
        //when coming back later in a separate session.
        //TODO: Keep a session without requiring a log in?
        if (userIsLoggedIn()) {
            getView().addUserAnswer(event.getQuestion(), event.getAnswerIsCorrect());
        }
    }

    private boolean userIsLoggedIn() {
        return userIsLoggedIn;
    }

    @ProxyEvent
    @Override
    public void onQuestionNextSectionId(@NotNull final QuestionNextQuestionSectionIdEvent event) {
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
        @NotNull final AsyncCallback<UserRecentHistory> callback = new AsyncCallback<UserRecentHistory>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                Log.error("AsyncCallback Failed: getUserRecentHistory(): " + caught.getMessage());
                onFailureGeneric();
            }

            @Override
            public void onSuccess(final UserRecentHistory result) {
                if (result == null) {
                    onFailureGeneric();
                }

                final LoginInfo loginInfo = result.getLoginInfo();
                if (loginInfo == null) {
                    onFailureGeneric();
                }

                userIsLoggedIn = loginInfo.isLoggedIn();
                getView().setUserRecentHistory(result, nextQuestionSectionId);
            }

            private void onFailureGeneric() {
                userIsLoggedIn = false;
                getView().setServerFailed();
            }
        };

        QuizServiceAsync.Util.getInstance().getUserRecentHistory(
                GWT.getHostPageBaseURL(), callback);
    }

}
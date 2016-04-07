package com.murrayc.bigoquiz.client.application.userhistorysections;


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
import com.murrayc.bigoquiz.client.UserHistory;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
import com.murrayc.bigoquiz.client.application.question.QuestionUserAnswerAddedEvent;
import com.murrayc.bigoquiz.client.application.userhistory.UserHistoryResetSectionsEvent;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistorySectionsPresenter extends PresenterWidget<UserHistorySectionsPresenter.MyView>
        implements UserHistorySectionsUserEditUiHandlers,
        QuestionUserAnswerAddedEvent.EventHandler,
        QuestionContextEvent.EventHandler,
        UserHistoryResetSectionsEvent.EventHandler {

    private String nextQuestionSectionId = null;
    private boolean multipleChoice = true;
    private boolean userIsLoggedIn = false;
    private String quizId = null;

    public interface MyView extends View, HasUiHandlers<UserHistorySectionsUserEditUiHandlers> {
        /** Set a whole set of history.
         */
        void setUserRecentHistory(final String quizId, final UserHistory result, final String nextQuestionSectionId, boolean multipleChoice);

        /** Add a single item of history.
         * For instance, to avoid retrieving the whole history from the server,
         * if the new item is know already.
         */
        void addUserAnswer(final Question question, boolean answerIsCorrect);

        void setServerFailed();

        /**
         * Set details about the history presentation/links that are affected by the current question's UI.
         *
         * @param nextQuestionSectionId
         * @param multipleChoice
         */
        void setQuestionContext(final String nextQuestionSectionId, boolean multipleChoice);
    }

    @Inject
    UserHistorySectionsPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        getView().setUiHandlers(this);

        addRegisteredHandler(QuestionUserAnswerAddedEvent.TYPE, this);
        addRegisteredHandler(QuestionContextEvent.TYPE, this);
        addRegisteredHandler(UserHistoryResetSectionsEvent.TYPE, this);
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
    public void onQuestionContextChanged(@NotNull final QuestionContextEvent event) {
        final String quizId = event.getQuizId();
        Log.fatal("debug: onQuestionContextChanged(): quizId=" + quizId);
        final String nextQuestionSectionId = event.getNextQuestionSectionId();
        final boolean multipleChoice = event.getMultipleChoice();
        final boolean quizChanged = !StringUtils.equals(this.quizId, quizId);
        final boolean nextSectionChanged = !StringUtils.equals(this.nextQuestionSectionId, nextQuestionSectionId);
        final boolean multipleChoiceChanged = this.multipleChoice != multipleChoice;
        setQuizId(quizId);
        this.nextQuestionSectionId = nextQuestionSectionId;
        this.multipleChoice = multipleChoice;

        if (quizChanged) {
            //Completely refresh the data from the server:
            getAndShowHistory();
        } else if (nextSectionChanged || multipleChoiceChanged) {
            //Just refresh the links:
            getView().setQuestionContext(nextQuestionSectionId, multipleChoice);
        }
    }

    @ProxyEvent
    @Override
    public void onUserProfileResetSections(final UserHistoryResetSectionsEvent event) {
        //Completely refresh the data from the server:
        getAndShowHistory();
    }

    private void getAndShowHistory() {
        final String quizId = getQuizId();
        if (StringUtils.isEmpty(quizId)) {
            getView().setUserRecentHistory(null, null, null, false);
            return;
        }

        @NotNull final AsyncCallback<UserHistory> callback = new AsyncCallback<UserHistory>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //The quizID must be invalid,
                    //so try the default one instead.
                    //TODO: Do nothing, assuming that the main content presenter will offer a list of quizzes?
                   Log.error("AsyncCallback Failed with IllegalArgumentException: getUserRecentHistory()", ex);
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getUserRecentHistory()", ex);
                    onFailureGeneric();
                }
            }

            @Override
            public void onSuccess(final UserHistory result) {
                if (result == null) {
                    onFailureGeneric();
                }

                final LoginInfo loginInfo = result.getLoginInfo();
                if (loginInfo == null) {
                    onFailureGeneric();
                }

                userIsLoggedIn = loginInfo.isLoggedIn();
                getView().setUserRecentHistory(quizId, result, nextQuestionSectionId, multipleChoice);

                tellParentPresenterAboutQuizTitle(quizId, result.getQuizTitle());
            }

            private void onFailureGeneric() {
                userIsLoggedIn = false;
                getView().setServerFailed();
            }
        };

        QuizServiceAsync.Util.getInstance().getUserRecentHistory(
                quizId, GWT.getHostPageBaseURL(), callback);
    }

    private String getQuizId() {
        return quizId;
    }

    private void setQuizId(final String quizId) {
        this.quizId = quizId;
    }

    private void tellParentPresenterAboutQuizTitle(final String quizId, final String quizTitle) {
        UserHistorySectionsTitleRetrievedEvent.fire(this, quizId, quizTitle);
    }
}
package com.murrayc.bigoquiz.client.application.userhistorysections;


import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyEvent;
import com.murrayc.bigoquiz.client.*;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.HttpStatusCodes;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
import com.murrayc.bigoquiz.client.application.question.QuestionUserAnswerAddedEvent;
import com.murrayc.bigoquiz.client.application.userhistory.UserHistoryResetSectionsEvent;
import com.murrayc.bigoquiz.shared.Question;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.FailedResponseException;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
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
    private boolean userIsLoggedIn = false;
    private String quizId = null;

    public interface MyView extends View, HasUiHandlers<UserHistorySectionsUserEditUiHandlers> {
        /** Set a whole set of history.
         */
        void setUserRecentHistory(final String quizId, final UserHistorySections result, final String nextQuestionSectionId);

        /** Add a single item of history.
         * For instance, to avoid retrieving the whole history from the server,
         * if the new item is know already.
         */
        void addUserAnswer(final Question question, boolean answerIsCorrect);

        void setServerFailed();
        void setServerFailedUnknownQuiz();

        /**
         * Set details about the history presentation/links that are affected by the current question's UI.
         *  @param nextQuestionSectionId
         *
         */
        void setQuestionContext(final String nextQuestionSectionId);
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
        final String nextQuestionSectionId = event.getNextQuestionSectionId();
        final boolean quizChanged = !StringUtils.equals(this.quizId, quizId);
        final boolean nextSectionChanged = !StringUtils.equals(this.nextQuestionSectionId, nextQuestionSectionId);
        setQuizId(quizId);
        this.nextQuestionSectionId = nextQuestionSectionId;

        if (quizChanged) {
            //Completely refresh the data from the server:
            getAndShowHistory();
        } else if (nextSectionChanged) {
            //Just refresh the links:
            getView().setQuestionContext(nextQuestionSectionId);
        }
    }

    @ProxyEvent
    @Override
    public void onUserProfileResetSections(final UserHistoryResetSectionsEvent event) {
        //Completely refresh the data from the server:
        getAndShowHistory();
    }

    private void getAndShowHistory() {
        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        final UserHistoryClient client = GWT.create(UserHistoryClient.class);

        final String quizId = getQuizId();
        if (StringUtils.isEmpty(quizId)) {
            getView().setUserRecentHistory(null, null, null);
            return;
        }

        @NotNull final MethodCallback<UserHistorySections> callback = new MethodCallback<UserHistorySections>() {
            @Override
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                try {
                    userIsLoggedIn = false;
                    throw caught;
                } catch (final FailedResponseException ex) {
                    Log.error("submitDontKnowAnswer(): AsyncCallback failed with status code: " + ex.getStatusCode());
                    showErrorInView(getView(), ex);
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getUserRecentHistory()", ex);
                    onFailureGeneric();
                }
            }

            @Override
            public void onSuccess(final Method method, final UserHistorySections result) {
                if (result == null) {
                    onFailureGeneric();
                }

                final LoginInfo loginInfo = result.getLoginInfo();
                if (loginInfo == null) {
                    onFailureGeneric();
                }

                userIsLoggedIn = loginInfo.isLoggedIn();
                getView().setUserRecentHistory(quizId, result, nextQuestionSectionId);

                tellParentPresenterAboutQuizTitle(quizId, result.getQuizTitle());
            }

            private void onFailureGeneric() {
                userIsLoggedIn = false;
                getView().setServerFailed();
                Utils.tellUserHistoryPresenterAboutNoQuestionContext(UserHistorySectionsPresenter.this); //clear the sections sidebar.
            }

            /*
            // TODO: Map the 404 to this:
            private void onFailureUnknownQuiz() {
                userIsLoggedIn = false;
                getView().setServerFailedUnknownQuiz();
                Utils.tellUserHistoryPresenterAboutNoQuestionContext(UserHistorySectionsPresenter.this); //clear the sections sidebar.

            }
            */
        };

        client.getByQuizId(quizId, Window.Location.getHref(), callback);
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

    protected static void showErrorInView(final MyView view, final FailedResponseException ex) {
        if (ex.getStatusCode() == HttpStatusCodes.NOT_FOUND) {
            //One of the parameters (quizID, questionId, etc) must be invalid
            view.setServerFailedUnknownQuiz();
        } else {
            view.setServerFailed();
        }
    }
}
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
import com.murrayc.bigoquiz.client.application.DefaultUserHistoryRequestEvent;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
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
        QuestionUserAnswerAddedEvent.EventHandler,
        QuestionContextEvent.EventHandler,
        UserProfileResetSectionsEvent.EventHandler,
        DefaultUserHistoryRequestEvent.EventHandler {

    public static final String DEFAULT_QUIZ_ID = "bigoquiz";
    private String nextQuestionSectionId;
    private boolean multipleChoice = true;
    private boolean userIsLoggedIn = false;
    private String quizId;

    public interface MyView extends View, HasUiHandlers<UserHistoryRecentUserEditUiHandlers> {
        /** Set a whole set of history.
         */
        void setUserRecentHistory(final String quizId, final UserRecentHistory result, final String nextQuestionSectionId, boolean multipleChoice);

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
    UserHistoryRecentPresenter(
            EventBus eventBus,
            MyView view) {
        super(eventBus, view);

        getView().setUiHandlers(this);

        addRegisteredHandler(QuestionUserAnswerAddedEvent.TYPE, this);
        addRegisteredHandler(QuestionContextEvent.TYPE, this);
        addRegisteredHandler(UserProfileResetSectionsEvent.TYPE, this);
        addRegisteredHandler(DefaultUserHistoryRequestEvent.TYPE, this);
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
        final boolean multipleChoice = event.getMultipleChoice();
        final boolean quizChanged = !StringUtils.equals(this.quizId, quizId);
        final boolean nextSectionChanged = !StringUtils.equals(this.nextQuestionSectionId, nextQuestionSectionId);
        final boolean multipleChoiceChanged = this.multipleChoice != multipleChoice;
        this.quizId = quizId;
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
    public void onUserProfileResetSections(final UserProfileResetSectionsEvent event) {
        //Completely refresh the data from the server:
        getAndShowHistory();
    }

    @ProxyEvent
    @Override
    public void onDefaultUserHistoryRequested(final DefaultUserHistoryRequestEvent event) {
        //Show _something_ rather than nothing.
        //TODO: This won't make sense when we really have multiple quizes.
        //Then we probably won't want to even show this sidebar on the general pages such as About.
        if (StringUtils.isEmpty(quizId)) {
            quizId = DEFAULT_QUIZ_ID;
            getAndShowHistory();
        }
    }

    private void getAndShowHistory() {
        @NotNull final AsyncCallback<UserRecentHistory> callback = new AsyncCallback<UserRecentHistory>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //The quizID must be invalid,
                    //so try the default one instead.
                    //TODO: Do nothing, assuming that the main content presenter will offer a list of quizzes?
                    if (!StringUtils.equals(quizId, DEFAULT_QUIZ_ID)) {
                        quizId = DEFAULT_QUIZ_ID;
                        getAndShowHistory();
                    } else {
                        Log.error("AsyncCallback Failed: getUserRecentHistory()", ex);
                    }
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getUserRecentHistory()", ex);
                    onFailureGeneric();
                }
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
                getView().setUserRecentHistory(getQuizId(), result, nextQuestionSectionId, multipleChoice);
            }

            private void onFailureGeneric() {
                userIsLoggedIn = false;
                getView().setServerFailed();
            }
        };

        QuizServiceAsync.Util.getInstance().getUserRecentHistory(
                getQuizId(), GWT.getHostPageBaseURL(), callback);
    }

    private String getQuizId() {
        return quizId;
    }

}
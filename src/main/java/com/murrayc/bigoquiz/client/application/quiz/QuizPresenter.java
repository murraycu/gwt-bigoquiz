package com.murrayc.bigoquiz.client.application.quiz;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.UnknownQuizException;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.Utils;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsPresenter;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizPresenter extends Presenter<QuizPresenter.MyView, QuizPresenter.MyProxy>
        implements QuizUserEditUiHandlers {
    //Put this in a shared PresenterWithUserHistoryRecent class, also used by QuizPresenter?
    private final UserHistorySectionsPresenter userHistorySectionsPresenter;
    public static final SingleSlot<UserHistorySectionsPresenter> SLOT_USER_HISTORY_RECENT = new SingleSlot();
    private final PlaceManager placeManager;

    private String quizId = null;

    interface MyView extends ContentView, HasUiHandlers<QuizUserEditUiHandlers> {
        void setQuiz(final Quiz quiz);
    }

    @ProxyStandard
    @NameToken(NameTokens.QUIZ)
    interface MyProxy extends ProxyPlace<QuizPresenter> {
    }

    @Inject
    QuizPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager,
            UserHistorySectionsPresenter userHistorySectionsPresenter) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        this.placeManager = placeManager;

        this.userHistorySectionsPresenter = userHistorySectionsPresenter;

        getView().setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        super.onBind();

        setInSlot(SLOT_USER_HISTORY_RECENT, userHistorySectionsPresenter);
    }

    @Override
    public void prepareFromRequest(@NotNull final PlaceRequest request) {
        super.prepareFromRequest(request);

        //Quiz ID:
        this.quizId = request.getParameter(NameTokens.PARAM_QUIZ_ID, null);
        if (StringUtils.isEmpty(quizId)) {
            //Default to bigoquiz.
            this.quizId = "bigoquiz";
            //TODO: Take the user to a list of quizzes.
        }

        getAndUseQuiz();
    }

    @Override
    public void onReset() {
        //Make sure that the sidebar's links are updated too:
        tellUserHistoryPresenterAboutQuestionContext();
    }

    private void tellUserHistoryPresenterAboutQuestionContext() {
        QuestionContextEvent.fire(this, getQuizId(), null, true);
    }

    private String getQuizId() {
        return quizId;
    }

    private void getAndUseQuiz() {
        @NotNull final AsyncCallback<Quiz> callback = new AsyncCallback<Quiz>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                getView().setLoadingLabelVisible(false);
                getView().setQuiz(null); //clear the previous quiz.
                Utils.tellUserHistoryPresenterAboutNoQuestionContext(QuizPresenter.this); //clear the sections sidebar.

                try {
                    throw caught;
                } catch (final UnknownQuizException ex) {
                    Log.error("AsyncCallback Failed with UnknownQuizException: getQuiz()", ex);
                    getView().setServerFailedUnknownQuiz();
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: getQuiz()", ex);
                    getView().setServerFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getQuiz()", ex);
                    getView().setServerFailed();
                }
            }

            @Override
            public void onSuccess(final Quiz result) {
                getView().setLoadingLabelVisible(false);

                getView().setQuiz(result);
            }
        };


        getView().setLoadingLabelVisible(true);
        QuizServiceAsync.Util.getInstance().getQuiz(getQuizId(), callback);
    }

    @Override
    public void onAnswerQuestions() {
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuizQuestion(getQuizId());
        placeManager.revealPlace(placeRequest);
    }
}
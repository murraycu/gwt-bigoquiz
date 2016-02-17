package com.murrayc.bigoquiz.client.application.quiz;


import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.QuizServiceAsync;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
import com.murrayc.bigoquiz.shared.Quiz;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class QuizPresenter extends Presenter<QuizPresenter.MyView, QuizPresenter.MyProxy> {
    private final PlaceManager placeManager;
    private String quizId;;

    interface MyView extends View {
        void setQuiz(final Quiz quiz);

        void setServerFailed();
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
            PlaceManager placeManager) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        this.placeManager = placeManager;
    }

    @Override
    public void prepareFromRequest(@NotNull final PlaceRequest request) {
        super.prepareFromRequest(request);

        //Quiz ID:
        this.quizId = request.getParameter(NameTokens.QUESTION_PARAM_QUIZ_ID, null);
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
        QuestionContextEvent.fire(this, getQuizId(), null);
    }

    private String getQuizId() {
        return quizId;
    }

    private void getAndUseQuiz() {
        @NotNull final AsyncCallback<Quiz> callback = new AsyncCallback<Quiz>() {
            @Override
            public void onFailure(@NotNull final Throwable caught) {
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: getQuiz(): " + ex.getMessage());
                    getView().setServerFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: getQuiz(): " + ex.getMessage());
                    getView().setServerFailed();
                }
            }

            @Override
            public void onSuccess(final Quiz result) {
                getView().setQuiz(result);
            }
        };


        QuizServiceAsync.Util.getInstance().getQuiz(getQuizId(), callback);
    }
}
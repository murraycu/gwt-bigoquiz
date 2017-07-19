package com.murrayc.bigoquiz.client.application.userhistory;


import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.Log;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.question.QuestionContextEvent;
import com.murrayc.bigoquiz.client.application.quiz.BigOQuizPresenter;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistoryClient;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsPresenter;
import com.murrayc.bigoquiz.client.application.userhistorysections.UserHistorySectionsTitleRetrievedEvent;
import com.murrayc.bigoquiz.shared.StringUtils;
import org.fusesource.restygwt.client.Defaults;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.MethodCallback;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class UserHistoryPresenter extends BigOQuizPresenter<UserHistoryPresenter.MyView, UserHistoryPresenter.MyProxy>
        implements UserHistoryUserEditUiHandlers,
        UserHistorySectionsTitleRetrievedEvent.EventHandler {
    //Put this in a shared PresenterWithUserHistoryRecent class, also used by QuizPresenter?
    private final UserHistorySectionsPresenter userHistorySectionsPresenter;
    public static final SingleSlot<UserHistorySectionsPresenter> SLOT_USER_HISTORY_RECENT = new SingleSlot<>();

    private String quizId;

    interface MyView extends ContentView, HasUiHandlers<UserHistoryUserEditUiHandlers> {
        void setQuizTitle(final String quizTitle);
        void setServerFailed();
    }

    @ProxyStandard
    @NameToken(NameTokens.USER_HISTORY)
    interface MyProxy extends ProxyPlace<UserHistoryPresenter> {
    }

    @Inject
    UserHistoryPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            UserHistorySectionsPresenter userHistorySectionsPresenter) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);

        this.userHistorySectionsPresenter = userHistorySectionsPresenter;

        getView().setUiHandlers(this);

        addRegisteredHandler(UserHistorySectionsTitleRetrievedEvent.TYPE, this);
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

        getAndUseQuizHistory();
    }

    private String getQuizId() {
        return quizId;
    }

    private void getAndUseQuizHistory() {
        tellUserHistoryPresenterAboutQuestionContext();
    }

    private void tellUserHistoryPresenterAboutQuestionContext() {
        QuestionContextEvent.fire(this, getQuizId(), null);
    }

    @Override
    public void onResetSections() {
        Defaults.setServiceRoot(GWT.getHostPageBaseURL());
        final UserHistoryClient client = GWT.create(UserHistoryClient.class);

        client.resetSections(getQuizId(), new MethodCallback<Void>() {
            @Override
            public void onFailure(final Method method, @NotNull final Throwable caught) {
                try {
                    throw caught;
                } catch (final IllegalArgumentException ex) {
                    //One of the parameters (quizID, questionId, etc) must be invalid,
                    //TODO: Handle this properly.
                    Log.error("AsyncCallback Failed with IllegalArgumentException: resetSections()", ex);
                    //TODO: getView().setUserStatusFailed();
                } catch (final Throwable ex) {
                    Log.error("AsyncCallback Failed: resetSections()", ex);
                    //TODO: getView().setUserStatusFailed();
                }
            }

            @Override
            public void onSuccess(final Method method, Void result) {
                tellUserHistorySectionsPresenterAboutResetSections();
            }
        });
    }

    private void tellUserHistorySectionsPresenterAboutResetSections() {
        UserHistoryResetSectionsEvent.fire(this);
    }

    @Override
    public void onQuizTitleRetrieved(final UserHistorySectionsTitleRetrievedEvent event) {
        //UserHistorySectionsPresenter knows the title and tells us here.
        if (StringUtils.equals(getQuizId(), event.getQuizId())) {
            getView().setQuizTitle(event.getQuizTitle());
        }
    }
}
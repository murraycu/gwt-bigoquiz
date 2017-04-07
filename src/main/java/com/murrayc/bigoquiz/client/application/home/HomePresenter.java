package com.murrayc.bigoquiz.client.application.home;


import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter;
import com.murrayc.bigoquiz.client.application.ContentView;
import com.murrayc.bigoquiz.client.application.PlaceUtils;
import com.murrayc.bigoquiz.client.application.quiz.BigOQuizPresenter;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/21/16.
 */
public class HomePresenter extends BigOQuizPresenter<HomePresenter.MyView, HomePresenter.MyProxy>
        implements HomeUserEditUiHandlers {
    private final PlaceManager placeManager;

    public interface MyView extends ContentView, HasUiHandlers<HomeUserEditUiHandlers> {
    }

    @ProxyStandard
    @NameToken(NameTokens.HOME)
    interface MyProxy extends ProxyPlace<HomePresenter> {
    }

    @Inject
    HomePresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            PlaceManager placeManager) {
        super(eventBus, view, proxy, ApplicationPresenter.SLOT_CONTENT);
        this.placeManager = placeManager;

        getView().setUiHandlers(this);
    }

    @Override
    public void onAnswerQuestions(final String quizId) {
        @NotNull final PlaceRequest placeRequest = PlaceUtils.getPlaceRequestForQuizQuestion(quizId);
        placeManager.revealPlace(placeRequest);
    }
}
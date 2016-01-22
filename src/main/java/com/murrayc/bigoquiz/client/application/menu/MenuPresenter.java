package com.murrayc.bigoquiz.client.application.menu;


import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

/**
 * Created by murrayc on 1/21/16.
 */
public class MenuPresenter extends PresenterWidget<MenuPresenter.MyView>
        implements MenuUserEditUiHandlers {
    private final PlaceManager placeManager;

    public interface MyView extends View, HasUiHandlers<MenuUserEditUiHandlers> {
    }

    @Inject
    MenuPresenter(
            EventBus eventBus,
            MyView view,
            PlaceManager placeManager) {
        super(eventBus, view);
        this.placeManager = placeManager;

        getView().setUiHandlers(this);
    }

    @Override
    public void goTo(final String token) {
        PlaceRequest placeRequest = new PlaceRequest.Builder()
                .nameToken(token)
                /* .with(ParameterTokens.MODEL, model) */
                .build();

        placeManager.revealPlace(placeRequest);
    }
}
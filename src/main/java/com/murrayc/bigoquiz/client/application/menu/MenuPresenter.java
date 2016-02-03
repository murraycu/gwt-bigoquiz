package com.murrayc.bigoquiz.client.application.menu;


import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.murrayc.bigoquiz.client.application.userstatus.UserStatusPresenter;

/**
 * Created by murrayc on 1/21/16.
 */
public class MenuPresenter extends PresenterWidget<MenuPresenter.MyView>
        implements MenuUserEditUiHandlers {
    private final UserStatusPresenter userStatusPresenter;
    static final SingleSlot SLOT_USER_STATUS = new SingleSlot();

    private final PlaceManager placeManager;

    public interface MyView extends View, HasUiHandlers<MenuUserEditUiHandlers> {
    }

    @Inject
    MenuPresenter(
            EventBus eventBus,
            MyView view,
            PlaceManager placeManager,
            UserStatusPresenter userStatusPresenter) {
        super(eventBus, view);
        this.placeManager = placeManager;

        this.userStatusPresenter = userStatusPresenter;

        getView().setUiHandlers(this);
    }

    @Override
    protected void onBind() {
        super.onBind();

        setInSlot(MenuPresenter.SLOT_USER_STATUS, userStatusPresenter);
    }
}
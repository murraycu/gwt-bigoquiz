package com.murrayc.bigoquiz.client.application;

import com.google.inject.Inject;

import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.presenter.slots.NestedSlot;
import com.gwtplatform.mvp.client.presenter.slots.SingleSlot;
import com.gwtplatform.mvp.client.proxy.Proxy;

import com.murrayc.bigoquiz.client.application.ApplicationPresenter.MyView;
import com.murrayc.bigoquiz.client.application.ApplicationPresenter.MyProxy;
import com.murrayc.bigoquiz.client.application.menu.MenuPresenter;
import com.murrayc.bigoquiz.client.application.userhistoryrecent.UserHistoryRecentPresenter;

/**
 * Created by murrayc on 1/21/16.
 */
public class ApplicationPresenter extends Presenter<MyView, MyProxy> {
    private final MenuPresenter menuPresenter;

    interface MyView extends View {
    }

    @ProxyStandard
    interface MyProxy extends Proxy<ApplicationPresenter> {
    }

    //This will use some presenter that corresponds to a place (see NameTokens)
    //such as QuizListPresenter, QuestionPresenter, UserProfilePresenter, or AboutPresenter.
    public static final NestedSlot SLOT_CONTENT = new NestedSlot();

    //The MenuPresenter is are on every page.
    public static final SingleSlot<MenuPresenter> SLOT_MENU = new SingleSlot();

    @Inject
    ApplicationPresenter(
            EventBus eventBus,
            MyView view,
            MyProxy proxy,
            MenuPresenter menuPresenter) {
        super(eventBus, view, proxy, RevealType.Root);

        this.menuPresenter = menuPresenter;
    }

    @Override
    protected void onBind() {
        super.onBind();

        setInSlot(SLOT_MENU, menuPresenter);
    }
}

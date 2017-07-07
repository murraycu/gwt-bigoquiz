package com.murrayc.bigoquiz.client.application.quiz;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Window;
import com.google.inject.Singleton;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.proxy.NavigationEvent;
import com.gwtplatform.mvp.client.proxy.Proxy;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

/**
 * Copyright (c) 2016 Murray Cumming
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * <p>
 * Created by murrayc on 07.04.17.
 */


@Singleton
public abstract class BigOQuizPresenter<V extends View, Proxy_ extends Proxy<?>> extends Presenter<V, Proxy_> {
    public BigOQuizPresenter(EventBus eventBus, V view, Proxy_ proxy, GwtEvent.Type<RevealContentHandler<?>> slot) {
        super(eventBus, view, proxy, slot);

        // Scroll to the top of the window whenever we reveal a new place.
        // TODO: Would it instead be enough to just call Window.scrollTo() after
        // Presenter.revealPlace()? Maybe not, because the GWTP documentation suggests that
        // revealPlace() is async (it fires an event).
        eventBus.addHandler(NavigationEvent.getType(), navigationEvent -> Scheduler.get().scheduleDeferred((Command) () -> {
            // Making the window scroll to top on every page change
            Window.scrollTo(0, 0);
        }));
    }
}

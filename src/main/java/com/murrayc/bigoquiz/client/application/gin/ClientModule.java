package com.murrayc.bigoquiz.client.application.gin;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;
import com.gwtplatform.mvp.client.gin.DefaultModule;
import com.gwtplatform.mvp.shared.proxy.RouteTokenFormatter;
import com.murrayc.bigoquiz.client.NameTokens;
import com.murrayc.bigoquiz.client.application.ApplicationModule;

/**
 * Created by murrayc on 1/21/16.
 */
public class ClientModule extends AbstractPresenterModule {
    @Override
    protected void configure() {
        install(new DefaultModule.Builder()
                .tokenFormatter(RouteTokenFormatter.class)
                .defaultPlace(NameTokens.QUESTION)
                .errorPlace(NameTokens.QUESTION)
                .unauthorizedPlace(NameTokens.QUESTION)
                .build());

        install(new ApplicationModule());
    }
}
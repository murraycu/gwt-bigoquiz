package com.murrayc.bigoquiz.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Created by murrayc on 1/18/16.
 */
public interface LoginServiceAsync {
    void login(String requestUri, AsyncCallback<LoginInfo> async);

    /**
     * Utility class to get the RPC Async interface from client-side code
     */
    final class Util {
        private static LoginServiceAsync instance;

        public static LoginServiceAsync getInstance() {
            if (instance == null) {
                instance = GWT.create(LoginService.class);
            }
            return instance;
        }

        private Util() {
            // Utility class should not be instantiated
        }
    }
}

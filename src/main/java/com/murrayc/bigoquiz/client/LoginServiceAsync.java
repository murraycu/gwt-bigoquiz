package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.murrayc.bigoquiz.client.LoginInfo;

/**
 * Created by murrayc on 1/18/16.
 */
public interface LoginServiceAsync {
    public void login(String requestUri, AsyncCallback<LoginInfo> async);
}

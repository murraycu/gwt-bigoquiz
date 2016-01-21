package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Created by murrayc on 1/18/16.
 */
@RemoteServiceRelativePath("login")
public interface LoginService extends RemoteService {
    LoginInfo login(final String requestUri);
}

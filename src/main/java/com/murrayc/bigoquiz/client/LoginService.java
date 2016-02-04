package com.murrayc.bigoquiz.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/18/16.
 */
@RemoteServiceRelativePath("login-service")
public interface LoginService extends RemoteService {
    @NotNull LoginInfo login(final String requestUri);
}

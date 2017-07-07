package com.murrayc.bigoquiz.server.gwtrpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginService;
import com.murrayc.bigoquiz.server.ServiceUserUtils;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/18/16.
 */
public class LoginServiceImpl extends RemoteServiceServlet implements
        LoginService {

    @NotNull
    public LoginInfo login(final String requestUri) {
        return ServiceUserUtils.getLoginInfo(requestUri);
    }
}

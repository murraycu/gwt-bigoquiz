package com.murrayc.bigoquiz.server.gwtrpc;

import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.client.LoginService;
import org.jetbrains.annotations.NotNull;

/**
 * Created by murrayc on 1/18/16.
 */
public class LoginServiceImpl extends ServiceWithUser implements
        LoginService {

    @NotNull
    public LoginInfo login(final String requestUri) {
        return getLoginInfo(requestUri);
    }
}

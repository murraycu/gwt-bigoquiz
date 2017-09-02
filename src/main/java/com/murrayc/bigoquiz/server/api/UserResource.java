package com.murrayc.bigoquiz.server.api;

import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.server.ServiceUserUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

/**
 * Created by murrayc on 7/7/17.
 */
@Path("user")
public class UserResource {
    /**
     *
     * @return
     */
    @GET
    @Produces("application/json")
    public LoginInfo get() {
        // Get a login URI which will return us to the home page when it
        // has finished.
        // TODO: Avoid hard-coding this.
        return ServiceUserUtils.getLoginInfo("http://bigoquiz.com");
    }
}

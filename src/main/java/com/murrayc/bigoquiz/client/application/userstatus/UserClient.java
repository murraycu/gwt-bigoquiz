package com.murrayc.bigoquiz.client.application.userstatus;

import com.murrayc.bigoquiz.client.LoginInfo;
import org.fusesource.restygwt.client.MethodCallback;
import org.fusesource.restygwt.client.RestService;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;

/**
 * Created by murrayc on 7/8/17.
 */
@Path("/api/user")
public interface UserClient extends RestService {
    @GET
    public void get(MethodCallback<LoginInfo> callback);
}

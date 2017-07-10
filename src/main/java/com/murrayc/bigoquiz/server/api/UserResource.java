package com.murrayc.bigoquiz.server.api;

import com.murrayc.bigoquiz.client.LoginInfo;
import com.murrayc.bigoquiz.server.ServiceUserUtils;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

/**
 * Created by murrayc on 7/7/17.
 */
@Path("user")
public class UserResource {
    /**
     * requestUrl is the URL that the user should be returned to after logging in or out.
     * It corresponds to the destinationURL parameter to UserService.createLogoutURL():
     * https://cloud.google.com/appengine/docs/standard/java/javadoc/com/google/appengine/api/users/UserService.html#createLogoutURL-java.lang.String-java.lang.String-
     *
     * @return
     */
    @GET
    @Produces("application/json")
    public LoginInfo get(@QueryParam("requestUrl") String requestUrl) {
        return ServiceUserUtils.getLoginInfo(requestUrl);
    }
}

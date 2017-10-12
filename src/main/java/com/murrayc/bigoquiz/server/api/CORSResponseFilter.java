package com.murrayc.bigoquiz.server.api;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;
import java.io.IOException;

/** Allow browsers to do CORS pre-flight checks before trying to access our api/ REST resources.
 * Otherwise, browsers will simply refuse to access api/ unless the javascript was served from
 * the same (sub)domain. This lets us use api/ from beta.bigoquiz.com.
 *
 * Jersey finds this via the @Provider annotation.
 */
@Provider
public class CORSResponseFilter implements ContainerResponseFilter {
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        headers.add("Access-Control-Allow-Origin", "http://beta.bigoquiz.com");
        headers.add("Access-Control-Allow-Methods", "GET, POST");

        final MultivaluedMap<String, String> reqHeaders = requestContext.getHeaders();
        if (reqHeaders != null) {
            headers.add("Access-Control-Allow-Headers", reqHeaders.get("Access-Control-Allow-Headers"));
        }
    }
}

package com.smartcampus.smartcampusapi.filters;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * Logs every incoming HTTP request (method + URI) and every outgoing
 * response (status code). Implements both ContainerRequestFilter and
 * ContainerResponseFilter for full observability.
 *
 * @author DINITHA KALANSOORIYA
 */
@Provider
public class LoggingFilter implements ContainerRequestFilter, ContainerResponseFilter {

    private static final Logger LOGGER =
            Logger.getLogger(LoggingFilter.class.getName());

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        LOGGER.info("Request  → " + method + " " + uri);
    }

    @Override
    public void filter(ContainerRequestContext requestContext,
                       ContainerResponseContext responseContext) throws IOException {
        String method = requestContext.getMethod();
        String uri = requestContext.getUriInfo().getRequestUri().toString();
        int status = responseContext.getStatus();
        LOGGER.info("Response ← " + method + " " + uri + " : " + status);
    }
}

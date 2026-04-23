package com.smartcampus.smartcampusapi.exceptions;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Catch-all exception mapper. Intercepts any unhandled runtime error and
 * returns a generic HTTP 500 — never exposing raw Java stack traces.
 *
 * @author DINITHA KALANSOORIYA
 */
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER =
            Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable exception) {
        // Let JAX-RS WebApplicationExceptions pass through with their own status
        if (exception instanceof WebApplicationException) {
            Response original = ((WebApplicationException) exception).getResponse();
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("error", original.getStatusInfo().getReasonPhrase());
            body.put("message", exception.getMessage());
            body.put("status", original.getStatus());
            return Response.status(original.getStatus())
                    .entity(body)
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        LOGGER.log(Level.SEVERE, "Unhandled exception caught", exception);

        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Internal Server Error");
        error.put("message", "An unexpected error occurred");
        error.put("status", 500);

        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

package com.smartcampus.smartcampusapi.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps SensorUnavailableException to HTTP 403 Forbidden.
 * Triggered when a POST reading is attempted on a sensor in MAINTENANCE or OFFLINE status.
 *
 * @author DINITHA KALANSOORIYA
 */
@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Forbidden");
        error.put("message", exception.getMessage());
        error.put("status", 403);

        return Response.status(Response.Status.FORBIDDEN)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

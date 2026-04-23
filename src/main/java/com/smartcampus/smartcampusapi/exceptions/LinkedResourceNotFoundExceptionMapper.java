package com.smartcampus.smartcampusapi.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps LinkedResourceNotFoundException to HTTP 422 Unprocessable Entity.
 * Used when a reference inside a valid JSON payload points to a non-existent resource
 * (e.g., a sensor POST with a roomId that does not exist).
 *
 * @author DINITHA KALANSOORIYA
 */
@Provider
public class LinkedResourceNotFoundExceptionMapper
        implements ExceptionMapper<LinkedResourceNotFoundException> {

    @Override
    public Response toResponse(LinkedResourceNotFoundException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Unprocessable Entity");
        error.put("message", exception.getMessage());
        error.put("status", 422);

        return Response.status(422)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

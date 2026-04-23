package com.smartcampus.smartcampusapi.exceptions;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps RoomNotEmptyException to a 409 Conflict response.
 *
 * @author DINITHA KALANSOORIYA
 */
@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException exception) {
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("error", "Conflict");
        error.put("message", exception.getMessage());
        error.put("status", 409);

        return Response.status(Response.Status.CONFLICT)
                .entity(error)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}

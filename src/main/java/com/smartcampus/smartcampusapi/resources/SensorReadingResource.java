package com.smartcampus.smartcampusapi.resources;

import com.smartcampus.smartcampusapi.data.DataStore;
import com.smartcampus.smartcampusapi.exceptions.SensorUnavailableException;
import com.smartcampus.smartcampusapi.models.Sensor;
import com.smartcampus.smartcampusapi.models.SensorReading;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sub-resource for managing Sensor Readings.
 * Instantiated by SensorResource's sub-resource locator — NOT registered
 * directly in SmartCampusApplication.
 *
 * @author DINITHA KALANSOORIYA
 */
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorReadingResource {

    private final String sensorId;
    private final DataStore ds = DataStore.getInstance();

    public SensorReadingResource(String sensorId) {
        this.sensorId = sensorId;
    }

    @GET
    public Response getReadings() {
        List<SensorReading> readings = ds.getReadings(sensorId);
        return Response.ok(readings).build();
    }

    @GET
    @Path("/{id}")
    public Response getReadingById(@PathParam("id") String id) {
        List<SensorReading> readings = ds.getReadings(sensorId);
        SensorReading found = readings.stream()
                .filter(r -> id.equals(r.getId()))
                .findFirst().orElse(null);
        if (found == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Reading not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(found).build();
    }

    @POST
    public Response addReading(SensorReading reading, @Context UriInfo uriInfo) {
        Sensor sensor = ds.getSensor(sensorId);

        // Block readings on non-ACTIVE sensors (→ 403 Forbidden)
        if (!"ACTIVE".equalsIgnoreCase(sensor.getStatus())) {
            throw new SensorUnavailableException(
                    "Sensor " + sensorId + " is " + sensor.getStatus()
                    + " and cannot accept readings");
        }

        if (reading.getId() == null || reading.getId().isEmpty()) {
            reading.setId(UUID.randomUUID().toString());
        }
        if (reading.getTimestamp() == 0) {
            reading.setTimestamp(System.currentTimeMillis());
        }

        ds.addReading(sensorId, reading);

        // Side effect: update parent sensor's currentValue
        sensor.setCurrentValue(reading.getValue());

        URI location = uriInfo.getAbsolutePathBuilder().path(reading.getId()).build();
        return Response.created(location).entity(reading).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteReading(@PathParam("id") String id) {
        List<SensorReading> readings = ds.getReadings(sensorId);
        boolean removed = readings.removeIf(r -> id.equals(r.getId()));
        if (!removed) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Reading not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.noContent().build();
    }
}

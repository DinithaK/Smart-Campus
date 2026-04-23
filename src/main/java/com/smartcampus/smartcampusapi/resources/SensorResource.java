package com.smartcampus.smartcampusapi.resources;

import com.smartcampus.smartcampusapi.data.DataStore;
import com.smartcampus.smartcampusapi.exceptions.LinkedResourceNotFoundException;
import com.smartcampus.smartcampusapi.models.Room;
import com.smartcampus.smartcampusapi.models.Sensor;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST resource for managing Sensors.
 * Also acts as a sub-resource locator for SensorReadingResource.
 *
 * @author DINITHA KALANSOORIYA
 */
@Path("/sensors")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class SensorResource {

    private final DataStore ds = DataStore.getInstance();

    // ── GET all sensors (optional ?type= filter) ───────────────────
    @GET
    public Response getAllSensors(@QueryParam("type") String type) {
        List<Sensor> result = new ArrayList<>();
        for (Sensor s : ds.getSensors().values()) {
            if (type == null || type.equalsIgnoreCase(s.getType())) {
                result.add(s);
            }
        }
        return Response.ok(result).build();
    }

    // ── GET single sensor ──────────────────────────────────────────
    @GET
    @Path("/{id}")
    public Response getSensorById(@PathParam("id") String id) {
        Sensor sensor = ds.getSensor(id);
        if (sensor == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(sensor).build();
    }

    // ── POST create sensor ─────────────────────────────────────────
    @POST
    public Response createSensor(Sensor sensor, @Context UriInfo uriInfo) {
        if (sensor.getId() == null || sensor.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Sensor id is required\"}").build();
        }
        if (ds.getSensors().containsKey(sensor.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Sensor already exists: " + sensor.getId() + "\"}").build();
        }
        // Validate that the referenced room exists (→ 422 if missing)
        if (sensor.getRoomId() != null && !sensor.getRoomId().isEmpty()) {
            Room room = ds.getRoom(sensor.getRoomId());
            if (room == null) {
                throw new LinkedResourceNotFoundException(
                        "Room not found: " + sensor.getRoomId());
            }
            room.getSensorIds().add(sensor.getId());
        }
        ds.putSensor(sensor);
        URI location = uriInfo.getAbsolutePathBuilder().path(sensor.getId()).build();
        return Response.created(location).entity(sensor).build();
    }

    // ── PUT update sensor ──────────────────────────────────────────
    @PUT
    @Path("/{id}")
    public Response updateSensor(@PathParam("id") String id, Sensor updated) {
        Sensor existing = ds.getSensor(id);
        if (existing == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        existing.setType(updated.getType());
        existing.setStatus(updated.getStatus());
        existing.setCurrentValue(updated.getCurrentValue());
        if (updated.getRoomId() != null && !updated.getRoomId().equals(existing.getRoomId())) {
            Room newRoom = ds.getRoom(updated.getRoomId());
            if (newRoom == null) {
                throw new LinkedResourceNotFoundException(
                        "Room not found: " + updated.getRoomId());
            }
            if (existing.getRoomId() != null) {
                Room oldRoom = ds.getRoom(existing.getRoomId());
                if (oldRoom != null) oldRoom.getSensorIds().remove(id);
            }
            newRoom.getSensorIds().add(id);
            existing.setRoomId(updated.getRoomId());
        }
        return Response.ok(existing).build();
    }

    // ── DELETE sensor ──────────────────────────────────────────────
    @DELETE
    @Path("/{id}")
    public Response deleteSensor(@PathParam("id") String id) {
        Sensor sensor = ds.getSensor(id);
        if (sensor == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Sensor not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        if (sensor.getRoomId() != null) {
            Room room = ds.getRoom(sensor.getRoomId());
            if (room != null) room.getSensorIds().remove(id);
        }
        ds.deleteSensor(id);
        return Response.noContent().build();
    }

    // ── Sub-resource locator for sensor readings ───────────────────
    @Path("/{sensorId}/readings")
    public SensorReadingResource getReadingsSubResource(
            @PathParam("sensorId") String sensorId) {
        Sensor sensor = ds.getSensor(sensorId);
        if (sensor == null) {
            throw new WebApplicationException(
                    "Sensor not found: " + sensorId, Response.Status.NOT_FOUND);
        }
        return new SensorReadingResource(sensorId);
    }
}

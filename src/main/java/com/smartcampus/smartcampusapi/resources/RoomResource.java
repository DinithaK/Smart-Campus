package com.smartcampus.smartcampusapi.resources;

import com.smartcampus.smartcampusapi.data.DataStore;
import com.smartcampus.smartcampusapi.exceptions.RoomNotEmptyException;
import com.smartcampus.smartcampusapi.models.Room;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * REST resource for managing Rooms.
 *
 * @author DINITHA KALANSOORIYA
 */
@Path("/rooms")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class RoomResource {

    private final DataStore ds = DataStore.getInstance();

    @GET
    public Response getAllRooms() {
        List<Room> rooms = new ArrayList<>(ds.getRooms().values());
        return Response.ok(rooms).build();
    }

    @GET
    @Path("/{id}")
    public Response getRoomById(@PathParam("id") String id) {
        Room room = ds.getRoom(id);
        if (room == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        return Response.ok(room).build();
    }

    @POST
    public Response createRoom(Room room, @Context UriInfo uriInfo) {
        if (room.getId() == null || room.getId().isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("{\"error\":\"Room id is required\"}").build();
        }
        if (ds.getRooms().containsKey(room.getId())) {
            return Response.status(Response.Status.CONFLICT)
                    .entity("{\"error\":\"Room already exists: " + room.getId() + "\"}").build();
        }
        ds.putRoom(room);
        URI location = uriInfo.getAbsolutePathBuilder().path(room.getId()).build();
        return Response.created(location).entity(room).build();
    }

    @PUT
    @Path("/{id}")
    public Response updateRoom(@PathParam("id") String id, Room updated) {
        Room existing = ds.getRoom(id);
        if (existing == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        existing.setName(updated.getName());
        existing.setCapacity(updated.getCapacity());
        return Response.ok(existing).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteRoom(@PathParam("id") String id) {
        Room room = ds.getRoom(id);
        if (room == null) {
            Map<String, Object> err = new LinkedHashMap<>();
            err.put("error", "Not Found");
            err.put("message", "Room not found: " + id);
            err.put("status", 404);
            return Response.status(Response.Status.NOT_FOUND).entity(err).build();
        }
        boolean hasSensors = ds.getSensors().values().stream()
                .anyMatch(s -> id.equals(s.getRoomId()));
        if (hasSensors) {
            throw new RoomNotEmptyException(
                    "Cannot delete room " + id + ": sensors are still assigned to it");
        }
        ds.deleteRoom(id);
        return Response.noContent().build();
    }
}

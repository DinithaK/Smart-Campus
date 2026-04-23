package com.smartcampus.smartcampusapi.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * API discovery / HATEOAS endpoint.
 * Returns versioning, contact details, and links to primary resource collections.
 *
 * @author DINITHA KALANSOORIYA
 */
@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class DiscoveryResource {

    @GET
    public Response discover(@Context UriInfo uriInfo) {
        String baseUri = uriInfo.getBaseUri().toString();

        Map<String, Object> root = new LinkedHashMap<>();
        root.put("name", "Smart Campus API");
        root.put("version", "v1");
        root.put("description",
                "University Smart Campus Sensor & Room Management API");

        // Administrative contact
        Map<String, String> contact = new LinkedHashMap<>();
        contact.put("department", "Campus Facilities Management");
        contact.put("email", "smartcampus@westminster.ac.uk");
        root.put("contact", contact);

        // HATEOAS links to primary resource collections
        Map<String, String> links = new LinkedHashMap<>();
        links.put("self", baseUri);
        links.put("rooms", baseUri + "rooms");
        links.put("sensors", baseUri + "sensors");
        root.put("links", links);

        return Response.ok(root).build();
    }
}

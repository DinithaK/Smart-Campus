package com.smartcampus.smartcampusapi;

import com.smartcampus.smartcampusapi.exceptions.GlobalExceptionMapper;
import com.smartcampus.smartcampusapi.exceptions.LinkedResourceNotFoundExceptionMapper;
import com.smartcampus.smartcampusapi.exceptions.RoomNotEmptyExceptionMapper;
import com.smartcampus.smartcampusapi.exceptions.SensorUnavailableExceptionMapper;
import com.smartcampus.smartcampusapi.filters.LoggingFilter;
import com.smartcampus.smartcampusapi.resources.DiscoveryResource;
import com.smartcampus.smartcampusapi.resources.RoomResource;
import com.smartcampus.smartcampusapi.resources.SensorResource;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;

/**
 * JAX-RS Application entry point.
 *
 * Lifecycle: JAX-RS creates a new resource instance per request (request-scoped).
 * We use a static DataStore to share data safely across requests.
 */
@ApplicationPath("/api/v1")
public class SmartCampusApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> classes = new HashSet<>();

        // Resources (SensorReadingResource is a sub-resource of SensorResource —
        // instantiated via sub-resource locator, NOT registered here)
        classes.add(DiscoveryResource.class);
        classes.add(RoomResource.class);
        classes.add(SensorResource.class);

        // Exception Mappers
        classes.add(RoomNotEmptyExceptionMapper.class);
        classes.add(LinkedResourceNotFoundExceptionMapper.class);
        classes.add(SensorUnavailableExceptionMapper.class);
        classes.add(GlobalExceptionMapper.class);

        // Filters
        classes.add(LoggingFilter.class);

        return classes;
    }
}
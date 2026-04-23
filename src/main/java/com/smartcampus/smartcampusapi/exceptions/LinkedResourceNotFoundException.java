package com.smartcampus.smartcampusapi.exceptions;

/**
 * Thrown when a linked/referenced resource (e.g. a Room referenced by a Sensor) is not found.
 *
 * @author DINITHA KALANSOORIYA
 */
public class LinkedResourceNotFoundException extends RuntimeException {

    public LinkedResourceNotFoundException(String message) {
        super(message);
    }
}

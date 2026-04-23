package com.smartcampus.smartcampusapi.exceptions;

/**
 * Thrown when an operation is attempted on a sensor that is OFFLINE or in MAINTENANCE.
 *
 * @author DINITHA KALANSOORIYA
 */
public class SensorUnavailableException extends RuntimeException {

    public SensorUnavailableException(String message) {
        super(message);
    }
}

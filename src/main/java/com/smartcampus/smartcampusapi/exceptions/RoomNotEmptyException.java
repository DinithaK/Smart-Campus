package com.smartcampus.smartcampusapi.exceptions;

/**
 * Thrown when attempting to delete a Room that still has sensors assigned to it.
 *
 * @author DINITHA KALANSOORIYA
 */
public class RoomNotEmptyException extends RuntimeException {

    public RoomNotEmptyException(String message) {
        super(message);
    }
}

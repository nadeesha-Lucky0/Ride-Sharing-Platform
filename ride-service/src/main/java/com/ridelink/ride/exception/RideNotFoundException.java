package com.ridelink.ride.exception;

public class RideNotFoundException extends RuntimeException {
    public RideNotFoundException(String message) {
        super(message);
    }

    public RideNotFoundException(String id, boolean isId) {
        super("Ride with ID '" + id + "' was not found.");
    }
}

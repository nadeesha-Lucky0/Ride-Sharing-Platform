package com.ridelink.driver.exception;

public class VehicleNotFoundException extends RuntimeException {
    public VehicleNotFoundException(String message) {
        super(message);
    }

    public VehicleNotFoundException(String id, boolean isId) {
        super("Vehicle with ID '" + id + "' was not found.");
    }
}

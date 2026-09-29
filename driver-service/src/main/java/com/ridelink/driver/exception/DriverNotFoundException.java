package com.ridelink.driver.exception;

public class DriverNotFoundException extends RuntimeException {
    public DriverNotFoundException(String message) {
        super(message);
    }

    public DriverNotFoundException(String id, boolean isId) {
        super("Driver with ID '" + id + "' was not found.");
    }
}

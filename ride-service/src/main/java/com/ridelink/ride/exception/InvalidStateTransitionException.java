package com.ridelink.ride.exception;

import com.ridelink.ride.model.RideStatus;

public class InvalidStateTransitionException extends RuntimeException {

    private final RideStatus currentStatus;
    private final RideStatus targetStatus;

    public InvalidStateTransitionException(RideStatus currentStatus, RideStatus targetStatus) {
        super(String.format("Invalid ride status transition from '%s' to '%s'.", currentStatus, targetStatus));
        this.currentStatus = currentStatus;
        this.targetStatus = targetStatus;
    }

    public InvalidStateTransitionException(String message) {
        super(message);
        this.currentStatus = null;
        this.targetStatus = null;
    }

    public RideStatus getCurrentStatus() {
        return currentStatus;
    }

    public RideStatus getTargetStatus() {
        return targetStatus;
    }
}

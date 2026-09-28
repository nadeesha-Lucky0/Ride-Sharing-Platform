package com.ridelink.ride.statemachine;

import com.ridelink.ride.exception.InvalidStateTransitionException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class RideStateMachine {

    private static final Map<RideStatus, Set<RideStatus>> VALID_TRANSITIONS = new EnumMap<>(RideStatus.class);

    static {
        // From REQUESTED
        VALID_TRANSITIONS.put(RideStatus.REQUESTED, EnumSet.of(
                RideStatus.ASSIGNED,
                RideStatus.ACCEPTED,
                RideStatus.CANCELLED
        ));

        // From ASSIGNED
        VALID_TRANSITIONS.put(RideStatus.ASSIGNED, EnumSet.of(
                RideStatus.ACCEPTED,
                RideStatus.REQUESTED, // when driver rejects / timeout, returns to pool
                RideStatus.CANCELLED
        ));

        // From ACCEPTED
        VALID_TRANSITIONS.put(RideStatus.ACCEPTED, EnumSet.of(
                RideStatus.DRIVER_ARRIVED,
                RideStatus.IN_PROGRESS,
                RideStatus.CANCELLED
        ));

        // From DRIVER_ARRIVED
        VALID_TRANSITIONS.put(RideStatus.DRIVER_ARRIVED, EnumSet.of(
                RideStatus.IN_PROGRESS,
                RideStatus.CANCELLED
        ));

        // From IN_PROGRESS
        VALID_TRANSITIONS.put(RideStatus.IN_PROGRESS, EnumSet.of(
                RideStatus.COMPLETED,
                RideStatus.CANCELLED
        ));

        // Terminal states
        VALID_TRANSITIONS.put(RideStatus.COMPLETED, Collections.emptySet());
        VALID_TRANSITIONS.put(RideStatus.CANCELLED, Collections.emptySet());
    }

    /**
     * Checks whether a status transition is permitted from currentStatus to targetStatus.
     */
    public boolean isValidTransition(RideStatus currentStatus, RideStatus targetStatus) {
        if (currentStatus == null || targetStatus == null) {
            return false;
        }
        if (currentStatus == targetStatus) {
            return false; // Idempotent or no-op transitions rejected
        }
        Set<RideStatus> allowed = VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet());
        return allowed.contains(targetStatus);
    }

    /**
     * Asserts that transition is valid, throwing InvalidStateTransitionException if not.
     */
    public void validateTransition(RideStatus currentStatus, RideStatus targetStatus) {
        if (!isValidTransition(currentStatus, targetStatus)) {
            throw new InvalidStateTransitionException(currentStatus, targetStatus);
        }
    }

    /**
     * Validates business invariants when transitioning a ride.
     */
    public void validateRideStateForTransition(Ride ride, RideStatus targetStatus, String driverId) {
        if (ride == null) {
            throw new IllegalArgumentException("Ride cannot be null.");
        }

        validateTransition(ride.getStatus(), targetStatus);

        // When transitioning to ASSIGNED or ACCEPTED, a driver ID must be present
        if ((targetStatus == RideStatus.ASSIGNED || targetStatus == RideStatus.ACCEPTED)) {
            String activeDriverId = driverId != null ? driverId : ride.getDriverId();
            if (activeDriverId == null || activeDriverId.isBlank()) {
                throw new InvalidStateTransitionException(
                        String.format("Cannot transition ride to '%s' without an assigned driverId.", targetStatus)
                );
            }
        }

        // When starting or completing a ride, driver must be assigned
        if (targetStatus == RideStatus.IN_PROGRESS || targetStatus == RideStatus.COMPLETED) {
            if (ride.getDriverId() == null || ride.getDriverId().isBlank()) {
                throw new InvalidStateTransitionException(
                        String.format("Cannot transition ride to '%s' without an assigned driver.", targetStatus)
                );
            }
        }
    }

    public Set<RideStatus> getAllowedNextStates(RideStatus currentStatus) {
        if (currentStatus == null) {
            return Collections.emptySet();
        }
        return Collections.unmodifiableSet(VALID_TRANSITIONS.getOrDefault(currentStatus, Collections.emptySet()));
    }
}

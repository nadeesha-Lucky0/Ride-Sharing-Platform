package com.ridelink.ride.statemachine;

import com.ridelink.ride.exception.InvalidStateTransitionException;
import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Ride State Machine Unit Tests")
class RideStateMachineTest {

    private RideStateMachine stateMachine;

    @BeforeEach
    void setUp() {
        stateMachine = new RideStateMachine();
    }

    @ParameterizedTest(name = "Valid transition from {0} to {1}")
    @CsvSource({
            "REQUESTED, ASSIGNED",
            "REQUESTED, ACCEPTED",
            "REQUESTED, CANCELLED",
            "ASSIGNED, ACCEPTED",
            "ASSIGNED, REQUESTED",
            "ASSIGNED, CANCELLED",
            "ACCEPTED, DRIVER_ARRIVED",
            "ACCEPTED, IN_PROGRESS",
            "ACCEPTED, CANCELLED",
            "DRIVER_ARRIVED, IN_PROGRESS",
            "DRIVER_ARRIVED, CANCELLED",
            "IN_PROGRESS, COMPLETED",
            "IN_PROGRESS, CANCELLED"
    })
    void isValidTransition_shouldReturnTrueForPermittedTransitions(RideStatus current, RideStatus target) {
        assertTrue(stateMachine.isValidTransition(current, target),
                () -> "Expected transition from " + current + " to " + target + " to be valid");
    }

    @ParameterizedTest(name = "Invalid transition from {0} to {1}")
    @CsvSource({
            "REQUESTED, COMPLETED",
            "REQUESTED, IN_PROGRESS",
            "REQUESTED, DRIVER_ARRIVED",
            "ASSIGNED, COMPLETED",
            "ASSIGNED, IN_PROGRESS",
            "ACCEPTED, COMPLETED",
            "ACCEPTED, REQUESTED",
            "COMPLETED, IN_PROGRESS",
            "COMPLETED, CANCELLED",
            "COMPLETED, REQUESTED",
            "CANCELLED, ACCEPTED",
            "CANCELLED, IN_PROGRESS",
            "CANCELLED, COMPLETED",
            "REQUESTED, REQUESTED",
            "IN_PROGRESS, IN_PROGRESS"
    })
    void isValidTransition_shouldReturnFalseForProhibitedTransitions(RideStatus current, RideStatus target) {
        assertFalse(stateMachine.isValidTransition(current, target),
                () -> "Expected transition from " + current + " to " + target + " to be invalid");
    }

    @Test
    void validateTransition_shouldThrowExceptionForInvalidTransition() {
        InvalidStateTransitionException ex = assertThrows(
                InvalidStateTransitionException.class,
                () -> stateMachine.validateTransition(RideStatus.REQUESTED, RideStatus.COMPLETED)
        );
        assertEquals(RideStatus.REQUESTED, ex.getCurrentStatus());
        assertEquals(RideStatus.COMPLETED, ex.getTargetStatus());
    }

    @Test
    void validateRideStateForTransition_shouldRequireDriverIdForAssignedStatus() {
        Ride ride = Ride.builder().status(RideStatus.REQUESTED).build();

        InvalidStateTransitionException ex = assertThrows(
                InvalidStateTransitionException.class,
                () -> stateMachine.validateRideStateForTransition(ride, RideStatus.ASSIGNED, null)
        );
        assertTrue(ex.getMessage().contains("without an assigned driverId"));
    }

    @Test
    void validateRideStateForTransition_shouldRequireDriverForInProgressStatus() {
        Ride ride = Ride.builder().status(RideStatus.ACCEPTED).driverId(null).build();

        InvalidStateTransitionException ex = assertThrows(
                InvalidStateTransitionException.class,
                () -> stateMachine.validateRideStateForTransition(ride, RideStatus.IN_PROGRESS, null)
        );
        assertTrue(ex.getMessage().contains("without an assigned driver"));
    }

    @Test
    void getAllowedNextStates_shouldReturnExpectedSet() {
        Set<RideStatus> allowed = stateMachine.getAllowedNextStates(RideStatus.IN_PROGRESS);
        assertTrue(allowed.contains(RideStatus.COMPLETED));
        assertTrue(allowed.contains(RideStatus.CANCELLED));
        assertFalse(allowed.contains(RideStatus.REQUESTED));
    }
}

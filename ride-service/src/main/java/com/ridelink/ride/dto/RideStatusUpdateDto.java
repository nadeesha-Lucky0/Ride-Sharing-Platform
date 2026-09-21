package com.ridelink.ride.dto;

import com.ridelink.ride.model.RideStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideStatusUpdateDto {

    @NotNull(message = "Status is required")
    private RideStatus status;

    private String driverId;

    private String cancellationReason;
}

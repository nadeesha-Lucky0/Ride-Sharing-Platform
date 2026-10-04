package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a payment request initiated for a ride.
 * Supports simulated multi-method payments with positive amount validation and negative failure simulation.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request payload to process or simulate a payment for a ride")
public class PaymentRequestDto {

    @NotBlank(message = "Ride ID is required")
    @Schema(description = "Unique identifier of the ride being paid for", example = "RIDE-1002")
    private String rideId;

    @NotBlank(message = "Passenger ID is required")
    @Schema(description = "Unique identifier of the passenger", example = "PASS-5501")
    private String passengerId;

    @NotBlank(message = "Driver ID is required")
    @Schema(description = "Unique identifier of the driver receiving the payment", example = "DRV-3309")
    private String driverId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @Schema(description = "Total payment amount (must be positive)", example = "350.50")
    private Double amount;

    @NotNull(message = "Payment method is required")
    @Schema(description = "Selected payment method (CARD, CASH, WALLET, UPI)", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Set to true to simulate payment gateway failure / card decline scenario", example = "false")
    private Boolean simulateFailure;

    @Schema(description = "Optional reason to specify for simulated failure", example = "Insufficient funds in bank account")
    private String failureReason;
}

package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object (DTO) representing a payment request initiated for a ride.
 * Contains necessary identifiers, transaction amount, and the chosen payment method.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestDto {

 
    @NotBlank(message = "Ride ID is required")
    private String rideId;
   
    @NotBlank(message = "Passenger ID is required")
    private String passengerId;
    
    @NotBlank(message = "Driver ID is required")
    private String driverId;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;
}

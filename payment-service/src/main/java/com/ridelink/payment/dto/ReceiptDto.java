package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing the payment receipt generated after a successful transaction.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDto {
    private String receiptNumber;     // Unique identifier for the receipt
    private String rideId;            // ID of the completed ride
    private String paymentId;         // ID of the associated payment transaction
    private Double totalAmount;       // Final total amount paid
    private Double baseFare;          // Initial base fare for the ride
    private Double distanceFare;      // Fare calculated based on distance traveled
    private Double timeFare;          // Fare calculated based on trip duration
    private Double taxAmount;         // Tax applied to the total fare
    private PaymentMethod paymentMethod;
    private LocalDateTime issuedAt;   
}

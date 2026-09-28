package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment transaction details and status")
public class PaymentResponseDto {

    @Schema(description = "Unique internal Payment ID", example = "65fe9823ab1234c90123")
    private String id;

    @Schema(description = "Associated ride ID", example = "RIDE-1002")
    private String rideId;

    @Schema(description = "Passenger identifier", example = "PASS-5501")
    private String passengerId;

    @Schema(description = "Driver identifier", example = "DRV-3309")
    private String driverId;

    @Schema(description = "Payment amount", example = "350.50")
    private Double amount;

    @Schema(description = "Currency denomination", example = "INR")
    private String currency;

    @Schema(description = "Payment method used", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Current payment status (PENDING, COMPLETED, FAILED, REFUNDED)", example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "Unique gateway transaction reference", example = "TXN-A7F28B9C")
    private String transactionReference;

    @Schema(description = "Failure reason if payment failed", example = "Card declined: insufficient funds")
    private String failureReason;

    @Schema(description = "Refund reason if payment was refunded", example = "Customer ride dispute")
    private String refundReason;

    @Schema(description = "Timestamp when refund was processed", example = "2026-09-27T10:30:00")
    private LocalDateTime refundedAt;

    @Schema(description = "Timestamp when payment record was created", example = "2026-09-27T10:15:00")
    private LocalDateTime createdAt;

    @Schema(description = "Timestamp when payment record was last modified", example = "2026-09-27T10:15:00")
    private LocalDateTime updatedAt;
}

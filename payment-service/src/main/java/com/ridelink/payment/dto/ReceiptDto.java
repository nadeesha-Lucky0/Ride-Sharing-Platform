package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import com.ridelink.payment.model.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Data Transfer Object (DTO) representing the digital payment receipt.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Digital ride receipt details")
public class ReceiptDto {

    @Schema(description = "Unique serial number of the receipt", example = "REC-93A1B2C3")
    private String receiptNumber;

    @Schema(description = "Associated ride identifier", example = "RIDE-1002")
    private String rideId;

    @Schema(description = "Associated payment record identifier", example = "65fe9823ab1234c90123")
    private String paymentId;

    @Schema(description = "Passenger identifier", example = "PASS-5501")
    private String passengerId;

    @Schema(description = "Driver identifier", example = "DRV-3309")
    private String driverId;

    @Schema(description = "Total amount paid", example = "350.50")
    private Double totalAmount;

    @Schema(description = "Base fare portion", example = "50.0")
    private Double baseFare;

    @Schema(description = "Distance fare portion", example = "213.0")
    private Double distanceFare;

    @Schema(description = "Time fare portion", example = "64.0")
    private Double timeFare;

    @Schema(description = "Surge pricing multiplier applied", example = "1.0")
    private Double surgeMultiplier;

    @Schema(description = "Tax amount charged", example = "17.52")
    private Double taxAmount;

    @Schema(description = "Discount deducted", example = "0.0")
    private Double discountAmount;

    @Schema(description = "Payment method used", example = "CARD")
    private PaymentMethod paymentMethod;

    @Schema(description = "Payment status at the time of receipt issuance", example = "COMPLETED")
    private PaymentStatus paymentStatus;

    @Schema(description = "Timestamp when receipt was issued", example = "2026-09-27T10:15:00")
    private LocalDateTime issuedAt;
}

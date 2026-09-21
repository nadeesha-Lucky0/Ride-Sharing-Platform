package com.ridelink.payment.dto;

import com.ridelink.payment.model.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptDto {
    private String receiptNumber;
    private String rideId;
    private String paymentId;
    private Double totalAmount;
    private Double baseFare;
    private Double distanceFare;
    private Double timeFare;
    private Double taxAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime issuedAt;
}

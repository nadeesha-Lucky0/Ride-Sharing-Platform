package com.ridelink.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "receipts")
public class Receipt {

    @Id
    private String id;

    @Indexed(unique = true)
    private String receiptNumber;

    @Indexed
    private String rideId;

    @Indexed
    private String paymentId;

    @Indexed
    private String passengerId;

    @Indexed
    private String driverId;

    private Double totalAmount;

    private Double baseFare;

    private Double distanceFare;

    private Double timeFare;

    private Double surgeMultiplier;

    private Double taxAmount;

    private Double discountAmount;

    private PaymentMethod paymentMethod;

    private PaymentStatus paymentStatus;

    @CreatedDate
    private LocalDateTime issuedAt;
}

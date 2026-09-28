package com.ridelink.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "payments")
public class Payment {

    @Id
    private String id;

    @Indexed
    private String rideId;

    @Indexed
    private String passengerId;

    @Indexed
    private String driverId;

    private Double amount;

    @Builder.Default
    private String currency = "INR";

    private PaymentMethod paymentMethod;

    @Indexed
    private PaymentStatus status;

    @Indexed(unique = true)
    private String transactionReference;

    private String failureReason;

    private String refundReason;

    private LocalDateTime refundedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}

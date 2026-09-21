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

    private String paymentId;

    private Double totalAmount;

    private Double baseFare;

    private Double distanceFare;

    private Double timeFare;

    private Double taxAmount;

    private PaymentMethod paymentMethod;

    @CreatedDate
    private LocalDateTime issuedAt;
}

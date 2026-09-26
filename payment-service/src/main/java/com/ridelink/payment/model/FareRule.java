package com.ridelink.payment.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "fare_rules")
public class FareRule {

    @Id
    private String id;

    private String vehicleCategory;

    private Double baseFare;

    private Double perKmRate;

    private Double perMinuteRate;

    private Double minimumFare;
}


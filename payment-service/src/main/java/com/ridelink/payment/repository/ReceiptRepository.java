package com.ridelink.payment.repository;

import com.ridelink.payment.model.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String> {
    Optional<Receipt> findByRideId(String rideId);
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
}

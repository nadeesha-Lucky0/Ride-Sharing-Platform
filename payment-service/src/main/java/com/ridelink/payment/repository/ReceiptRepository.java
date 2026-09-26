package com.ridelink.payment.repository;

import com.ridelink.payment.model.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Receipt documents in MongoDB.
 * Handles database operations for generated trip receipts.
 */
@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String> {

    /**
     * Retrieves a receipt associated with a specific ride ID.
     *
     * @param rideId the unique identifier of the ride
     * @return an Optional containing the Receipt if found, or empty otherwise
     */
    Optional<Receipt> findByRideId(String rideId);

    /**
     * Retrieves a receipt based on its unique receipt number.
     *
     * @param receiptNumber the unique serial or number of the receipt
     * @return an Optional containing the Receipt if found, or empty otherwise
     */
    Optional<Receipt> findByReceiptNumber(String receiptNumber);
}

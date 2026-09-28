package com.ridelink.payment.repository;

import com.ridelink.payment.model.Receipt;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Receipt documents in MongoDB.
 * Handles database operations for generated trip receipts.
 */
@Repository
public interface ReceiptRepository extends MongoRepository<Receipt, String> {

    /**
     * Retrieves a receipt associated with a specific ride ID.
     */
    Optional<Receipt> findByRideId(String rideId);

    /**
     * Retrieves a receipt based on its unique receipt number.
     */
    Optional<Receipt> findByReceiptNumber(String receiptNumber);

    /**
     * Retrieves a receipt based on its associated payment ID.
     */
    Optional<Receipt> findByPaymentId(String paymentId);

    /**
     * Retrieves all receipts issued for a specific passenger.
     */
    List<Receipt> findByPassengerId(String passengerId);

    /**
     * Retrieves all receipts issued for a specific driver.
     */
    List<Receipt> findByDriverId(String driverId);
}

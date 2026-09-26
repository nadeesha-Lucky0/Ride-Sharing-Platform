package com.ridelink.payment.repository;

import com.ridelink.payment.model.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing Payment documents in MongoDB.
 * Handles database operations related to ride payment transactions.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Retrieves a payment record associated with a specific ride ID.
     *
     * @param rideId the unique identifier of the ride
     * @return an Optional containing the Payment if found, or empty otherwise
     */
    Optional<Payment> findByRideId(String rideId);
}

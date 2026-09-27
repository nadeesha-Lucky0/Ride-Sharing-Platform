package com.ridelink.payment.repository;

import com.ridelink.payment.model.Payment;
import com.ridelink.payment.model.PaymentStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing Payment documents in MongoDB.
 * Handles database operations related to ride payment transactions and history.
 */
@Repository
public interface PaymentRepository extends MongoRepository<Payment, String> {

    /**
     * Retrieves a payment record associated with a specific ride ID.
     */
    Optional<Payment> findByRideId(String rideId);

    /**
     * Retrieves all payments associated with a specific ride ID.
     */
    List<Payment> findAllByRideId(String rideId);

    /**
     * Retrieves all payments initiated by a specific passenger.
     */
    List<Payment> findByPassengerId(String passengerId);

    /**
     * Retrieves all payments credited to a specific driver.
     */
    List<Payment> findByDriverId(String driverId);

    /**
     * Retrieves a payment by its unique gateway transaction reference.
     */
    Optional<Payment> findByTransactionReference(String transactionReference);

    /**
     * Retrieves payments filtered by status.
     */
    List<Payment> findByStatus(PaymentStatus status);
}

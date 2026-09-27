package com.ridelink.payment.repository;

import com.ridelink.payment.model.FareRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing FareRule documents in MongoDB.
 * Handles database operations for vehicle category fare configurations.
 */
@Repository
public interface FareRuleRepository extends MongoRepository<FareRule, String> {

    /**
     * Retrieves a fare rule based on the specified vehicle category (case-insensitive search can be supported in service).
     *
     * @param vehicleCategory the category of the vehicle (e.g., ECONOMY, PREMIUM, BIKE, AUTO, XL)
     * @return an Optional containing the FareRule if found, or empty otherwise
     */
    Optional<FareRule> findByVehicleCategoryIgnoreCase(String vehicleCategory);

    Optional<FareRule> findByVehicleCategory(String vehicleCategory);
}

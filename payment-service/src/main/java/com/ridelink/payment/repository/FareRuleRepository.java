package com.ridelink.payment.repository;

import com.ridelink.payment.model.FareRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for managing FareRule documents in MongoDB.
 * Provides standard CRUD operations as well as custom query methods for fare configurations.
 */
@Repository
public interface FareRuleRepository extends MongoRepository<FareRule, String> {

    /**
     * Retrieves a fare rule based on the specified vehicle category.
     *
     * @param vehicleCategory the category of the vehicle (e.g., ECONOMY, PREMIUM, BIKE)
     * @return an Optional containing the FareRule if found, or empty otherwise
     */
    Optional<FareRule> findByVehicleCategory(String vehicleCategory);
}

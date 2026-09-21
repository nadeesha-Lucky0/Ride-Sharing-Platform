package com.ridelink.payment.repository;

import com.ridelink.payment.model.FareRule;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FareRuleRepository extends MongoRepository<FareRule, String> {
    Optional<FareRule> findByVehicleCategory(String vehicleCategory);
}

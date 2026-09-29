package com.ridelink.driver.repository;

import com.ridelink.driver.model.Vehicle;
import com.ridelink.driver.model.VehicleCategory;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleRepository extends MongoRepository<Vehicle, String> {

    List<Vehicle> findByDriverId(String driverId);

    Optional<Vehicle> findFirstByDriverIdAndActiveTrue(String driverId);

    Optional<Vehicle> findByLicensePlate(String licensePlate);

    List<Vehicle> findByCategory(VehicleCategory category);

    void deleteByDriverId(String driverId);
}

package com.ridelink.ride.repository;

import com.ridelink.ride.model.Ride;
import com.ridelink.ride.model.RideStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RideRepository extends MongoRepository<Ride, String> {

    List<Ride> findByPassengerId(String passengerId);

    List<Ride> findByPassengerIdOrderByRequestedAtDesc(String passengerId);

    List<Ride> findByDriverId(String driverId);

    List<Ride> findByDriverIdOrderByRequestedAtDesc(String driverId);

    List<Ride> findByStatus(RideStatus status);

    List<Ride> findByStatusIn(List<RideStatus> statuses);
}

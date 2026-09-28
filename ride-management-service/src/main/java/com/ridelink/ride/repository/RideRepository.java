package com.ridelink.ride.repository;

import com.ridelink.ride.entity.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface RideRepository extends MongoRepository<Ride, String> {
    List<Ride> findByPassengerIdOrderByRequestedAtDesc(String passengerId);
    List<Ride> findByDriverIdOrderByRequestedAtDesc(String driverId);
}

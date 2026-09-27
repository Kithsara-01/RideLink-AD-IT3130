package com.ridelink.payment.repository;

import com.ridelink.payment.model.FinalFare;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FinalFareRepository
        extends MongoRepository<FinalFare, String> {

    Optional<FinalFare> findByRideId(String rideId);

    boolean existsByRideId(String rideId);
}
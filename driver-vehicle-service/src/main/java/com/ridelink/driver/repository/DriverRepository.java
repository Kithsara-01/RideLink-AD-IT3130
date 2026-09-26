package com.ridelink.driver.repository;

import com.ridelink.driver.model.AvailabilityStatus;
import com.ridelink.driver.model.Driver;
import com.ridelink.driver.model.OperationalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DriverRepository extends MongoRepository<Driver, String> {

    Optional<Driver> findByUserId(String userId);

    Optional<Driver> findByDriverLicenseNumber(String driverLicenseNumber);

    Optional<Driver> findByVehicleLicensePlate(String licensePlate);

    boolean existsByUserId(String userId);

    boolean existsByDriverLicenseNumber(String driverLicenseNumber);

    boolean existsByVehicleLicensePlate(String licensePlate);

    List<Driver> findByOperationalStatusAndAvailabilityStatus(
            OperationalStatus operationalStatus,
            AvailabilityStatus availabilityStatus
    );

    List<Driver> findByOperationalStatusAndAvailabilityStatusAndServiceAreaIgnoreCase(
            OperationalStatus operationalStatus,
            AvailabilityStatus availabilityStatus,
            String serviceArea
    );
}

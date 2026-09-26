package com.ridelink.driver.service;

import com.ridelink.driver.dto.AvailableDriverResponse;
import com.ridelink.driver.dto.CreateDriverRequest;
import com.ridelink.driver.dto.DriverResponse;
import com.ridelink.driver.dto.LocationDto;
import com.ridelink.driver.dto.UpdateAvailabilityRequest;
import com.ridelink.driver.dto.UpdateLocationRequest;
import com.ridelink.driver.dto.UpdateOperationalStatusRequest;
import com.ridelink.driver.dto.UpdateVehicleRequest;
import com.ridelink.driver.dto.VehicleDto;
import com.ridelink.driver.exception.ApiException;
import com.ridelink.driver.entity.AvailabilityStatus;
import com.ridelink.driver.entity.Driver;
import com.ridelink.driver.entity.Location;
import com.ridelink.driver.entity.OperationalStatus;
import com.ridelink.driver.entity.Vehicle;
import com.ridelink.driver.entity.VehicleType;
import com.ridelink.driver.repository.DriverRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class DriverService {

    private static final double EARTH_RADIUS_KM = 6371.0;
    private static final double DEFAULT_SEARCH_RADIUS_KM = 15.0;

    private final DriverRepository driverRepository;

    public DriverService(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    public DriverResponse registerDriver(CreateDriverRequest request) {
        String trimmedUserId = request.getUserId().trim();
        String normalizedLicenseNumber = request.getDriverLicenseNumber().trim().toUpperCase(Locale.ROOT);
        String normalizedPlate = request.getVehicle().getLicensePlate().trim().toUpperCase(Locale.ROOT);

        if (driverRepository.existsByUserId(trimmedUserId)) {
            throw new ApiException(HttpStatus.CONFLICT, "Driver profile already exists for user ID: " + trimmedUserId);
        }

        if (driverRepository.existsByDriverLicenseNumber(normalizedLicenseNumber)) {
            throw new ApiException(HttpStatus.CONFLICT, "Driver license number is already registered");
        }

        if (driverRepository.existsByVehicleLicensePlate(normalizedPlate)) {
            throw new ApiException(HttpStatus.CONFLICT, "Vehicle license plate is already registered");
        }

        VehicleDto vDto = request.getVehicle();
        Vehicle vehicle = new Vehicle(
            vDto.getMake().trim(),
            vDto.getModel().trim(),
            vDto.getYear(),
            normalizedPlate,
            vDto.getColor().trim(),
            vDto.getVehicleType(),
            vDto.getCapacity()
        );

        LocationDto lDto = request.getInitialLocation();
        Location location = new Location(
            lDto.getLatitude(),
            lDto.getLongitude(),
            lDto.getAddressOrPlace() != null ? lDto.getAddressOrPlace().trim() : "Initial Location",
            Instant.now()
        );

        Driver driver = new Driver(
            trimmedUserId,
            normalizedLicenseNumber,
            request.getPhoneNumber().trim(),
            request.getServiceArea().trim(),
            vehicle,
            location
        );

        Driver saved = driverRepository.save(driver);
        return DriverResponse.fromEntity(saved);
    }

    public DriverResponse getDriverById(String id) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));
        return DriverResponse.fromEntity(driver);
    }

    public DriverResponse getDriverByUserId(String userId) {
        Driver driver = driverRepository.findByUserId(userId.trim())
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found for user ID: " + userId));
        return DriverResponse.fromEntity(driver);
    }

    public DriverResponse updateVehicle(String id, UpdateVehicleRequest request) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));

        String normalizedPlate = request.getLicensePlate().trim().toUpperCase(Locale.ROOT);

        driverRepository.findByVehicleLicensePlate(normalizedPlate).ifPresent(existing -> {
            if (!existing.getId().equals(driver.getId())) {
                throw new ApiException(HttpStatus.CONFLICT, "Vehicle license plate is already registered to another driver");
            }
        });

        Vehicle vehicle = new Vehicle(
            request.getMake().trim(),
            request.getModel().trim(),
            request.getYear(),
            normalizedPlate,
            request.getColor().trim(),
            request.getVehicleType(),
            request.getCapacity()
        );

        driver.setVehicle(vehicle);
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse updateAvailability(String id, UpdateAvailabilityRequest request) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));

        AvailabilityStatus targetStatus = request.getStatus();

        if (driver.getOperationalStatus() == OperationalStatus.SUSPENDED && targetStatus == AvailabilityStatus.AVAILABLE) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Suspended driver cannot set availability status to AVAILABLE");
        }

        if (driver.getAvailabilityStatus() == AvailabilityStatus.BUSY && targetStatus == AvailabilityStatus.OFFLINE) {
            throw new ApiException(HttpStatus.CONFLICT, "Cannot go OFFLINE while engaged in an active ride");
        }

        driver.setAvailabilityStatus(targetStatus);
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse updateLocation(String id, UpdateLocationRequest request) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));

        Location location = driver.getCurrentLocation();
        if (location == null) {
            location = new Location();
        }

        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        if (request.getAddressOrPlace() != null && !request.getAddressOrPlace().isBlank()) {
            location.setAddressOrPlace(request.getAddressOrPlace().trim());
        }
        location.setLastUpdated(Instant.now());
        driver.setCurrentLocation(location);

        if (request.getServiceArea() != null && !request.getServiceArea().isBlank()) {
            driver.setServiceArea(request.getServiceArea().trim());
        }

        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse assignDriver(String id) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));

        if (driver.getOperationalStatus() == OperationalStatus.SUSPENDED) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Suspended driver cannot be assigned to rides");
        }

        if (driver.getAvailabilityStatus() != AvailabilityStatus.AVAILABLE) {
            throw new ApiException(HttpStatus.CONFLICT, "Driver is not available for assignment. Current status: " + driver.getAvailabilityStatus());
        }

        driver.setAvailabilityStatus(AvailabilityStatus.BUSY);
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse releaseDriver(String id) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));

        driver.setAvailabilityStatus(AvailabilityStatus.AVAILABLE);
        int completed = driver.getTotalRidesCompleted() == null ? 0 : driver.getTotalRidesCompleted();
        driver.setTotalRidesCompleted(completed + 1);
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public DriverResponse updateOperationalStatus(String id, UpdateOperationalStatusRequest request) {
        Driver driver = driverRepository.findById(id)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Driver not found with ID: " + id));

        driver.setOperationalStatus(request.getStatus());
        if (request.getStatus() == OperationalStatus.SUSPENDED) {
            driver.setAvailabilityStatus(AvailabilityStatus.OFFLINE);
        }
        driver.setUpdatedAt(Instant.now());

        Driver updated = driverRepository.save(driver);
        return DriverResponse.fromEntity(updated);
    }

    public List<AvailableDriverResponse> findAvailableDrivers(
            String serviceArea,
            VehicleType vehicleType,
            Double pickupLat,
            Double pickupLng,
            Double radiusKm) {

        List<Driver> activeAvailableDrivers = driverRepository.findByOperationalStatusAndAvailabilityStatus(
            OperationalStatus.ACTIVE,
            AvailabilityStatus.AVAILABLE
        );

        double maxRadius = (radiusKm != null && radiusKm > 0) ? radiusKm : DEFAULT_SEARCH_RADIUS_KM;

        return activeAvailableDrivers.stream()
            .filter(driver -> {
                if (serviceArea != null && !serviceArea.isBlank()) {
                    return driver.getServiceArea() != null &&
                           driver.getServiceArea().equalsIgnoreCase(serviceArea.trim());
                }
                return true;
            })
            .filter(driver -> {
                if (vehicleType != null) {
                    return driver.getVehicle() != null &&
                           driver.getVehicle().getVehicleType() == vehicleType;
                }
                return true;
            })
            .map(driver -> {
                Double distance = null;
                if (pickupLat != null && pickupLng != null && driver.getCurrentLocation() != null) {
                    Double dLat = driver.getCurrentLocation().getLatitude();
                    Double dLng = driver.getCurrentLocation().getLongitude();
                    if (dLat != null && dLng != null) {
                        distance = calculateDistanceKm(pickupLat, pickupLng, dLat, dLng);
                    }
                }
                return AvailableDriverResponse.fromEntity(driver, distance);
            })
            .filter(response -> {
                if (pickupLat != null && pickupLng != null) {
                    return response.getDistanceKm() != null && response.getDistanceKm() <= maxRadius;
                }
                return true;
            })
            .sorted(Comparator.comparing(AvailableDriverResponse::getDistanceKm, Comparator.nullsLast(Double::compareTo)))
            .collect(Collectors.toList());
    }

    public double calculateDistanceKm(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_KM * c;
    }
}

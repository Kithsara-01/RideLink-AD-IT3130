package com.ridelink.payment.exception;

import org.springframework.http.HttpStatus;

public class RideServiceException extends RuntimeException {

    private final HttpStatus status;

    public RideServiceException(
            HttpStatus status,
            String message) {

        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
package com.ridelink.payment.exception;

public class FinalFareAlreadyExistsException extends RuntimeException {

    public FinalFareAlreadyExistsException(String message) {
        super(message);
    }
}
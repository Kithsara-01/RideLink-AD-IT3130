package com.ridelink.payment.exception;

public class FinalFareNotFoundException extends RuntimeException {

    public FinalFareNotFoundException(String message) {
        super(message);
    }
}
package com.ridelink.payment.exception;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateKeyException(
            DuplicateKeyException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                "A duplicate record already exists");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        String message = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Invalid request");

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message);
    }

    @ExceptionHandler(FinalFareNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleFinalFareNotFound(
            FinalFareNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(FinalFareAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleFinalFareConflict(
            FinalFareAlreadyExistsException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(PaymentNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentNotFound(
            PaymentNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(ReceiptNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleReceiptNotFound(
            ReceiptNotFoundException ex) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                ex.getMessage());
    }

    @ExceptionHandler(InvalidPaymentStateException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidPaymentState(
            InvalidPaymentStateException ex) {

        return buildResponse(
                HttpStatus.CONFLICT,
                ex.getMessage());
    }

    @ExceptionHandler(PaymentAccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handlePaymentAccessDenied(
            PaymentAccessDeniedException ex) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                ex.getMessage());
    }

    @ExceptionHandler(RideServiceException.class)
    public ResponseEntity<Map<String, Object>> handleRideServiceException(
            RideServiceException ex) {

        return buildResponse(
                ex.getStatus(),
                ex.getMessage());
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String message) {

        Map<String, Object> body = new LinkedHashMap<>();

        body.put("timestamp", Instant.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);

        return ResponseEntity.status(status).body(body);
    }
}
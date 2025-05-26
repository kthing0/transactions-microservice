package org.kthing.transactionsmicroservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.io.FileNotFoundException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TransactionServiceException.class)
    public ResponseEntity<Object> handleTransactionServiceException(TransactionServiceException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Transaction service error", ex.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    public ResponseEntity<Object> handleFileNotFoundException(FileNotFoundException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.NOT_FOUND, "Transaction data file not found", ex.getMessage());
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        return createErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", ex.getMessage());
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        return createErrorResponse(HttpStatus.BAD_REQUEST, "Bad request", ex.getMessage());
    }
    
    private ResponseEntity<Object> createErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        
        return new ResponseEntity<>(body, status);
    }
}
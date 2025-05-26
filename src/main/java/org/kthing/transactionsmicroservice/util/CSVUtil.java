package org.kthing.transactionsmicroservice.util;

import org.apache.commons.csv.CSVRecord;
import org.kthing.transactionsmicroservice.exception.TransactionServiceException;
import org.kthing.transactionsmicroservice.model.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CSVUtil {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    
    private CSVUtil() {

    }
    
    public static Transaction parseTransactionRecord(CSVRecord record, Transaction.TransactionType type) {
        try {
            String transactionId = validateRequiredField(record, "TRANSACTION_ID");
            String customerId = validateRequiredField(record, "CUSTOMER_ID");
            String accountId = validateRequiredField(record, "ACCOUNT_ID");
            String amountStr = validateRequiredField(record, "AMOUNT");
            String dateTimeStr = validateRequiredField(record, "DATE_TIME");

            validateIdFormat(transactionId, "transaction ID");
            validateIdFormat(customerId, "customer ID");
            validateIdFormat(accountId, "account ID");

            BigDecimal amount = parseAmount(amountStr);
            LocalDateTime dateTime = parseDateTime(dateTimeStr);

            return Transaction.builder()
                    .transactionId(transactionId)
                    .customerId(customerId)
                    .accountId(accountId)
                    .amount(amount)
                    .dateTime(dateTime)
                    .type(type)
                    .build();
        } catch (Exception e) {
            if (e instanceof TransactionServiceException) {
                throw (TransactionServiceException) e;
            }
            throw new TransactionServiceException("Error parsing CSV record", e);
        }
    }
    private static void validateIdFormat(String id, String fieldName) {
        if (!id.matches("\\d+")) {
            throw new TransactionServiceException("Invalid " + fieldName + " format: " + id);
        }
    }
    
    private static BigDecimal parseAmount(String amountStr) {
        try {
            BigDecimal amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new TransactionServiceException("Amount must be positive: " + amountStr);
            }
            return amount;
        } catch (NumberFormatException e) {
            throw new TransactionServiceException("Invalid amount format: " + amountStr);
        }
    }
    
    private static LocalDateTime parseDateTime(String dateTimeStr) {
        try {
            return LocalDateTime.parse(dateTimeStr, DATE_TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            throw new TransactionServiceException("Invalid date time format: " + dateTimeStr);
        }
    }

    private static String validateRequiredField(CSVRecord record, String fieldName) {
        String value = record.get(fieldName);
        if (value == null || value.trim().isEmpty()) {
            throw new TransactionServiceException("Required field is missing or empty: " + fieldName);
        }
        return value.trim();
    }
}
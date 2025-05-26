package org.kthing.transactionsmicroservice.util;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.kthing.transactionsmicroservice.exception.TransactionServiceException;
import org.kthing.transactionsmicroservice.model.Transaction;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class PagedCsvReader {

    private static final String[] REQUIRED_HEADERS = {
        "TRANSACTION_ID", "CUSTOMER_ID", "ACCOUNT_ID", "AMOUNT", "DATE_TIME"
    };

    public static List<Transaction> readTransactions(String filePath, Transaction.TransactionType type, long startPosition, int maxCount, String accountId, LocalDateTime fromDate, LocalDateTime toDate) throws IOException {
        if (startPosition < 0) {
            throw new TransactionServiceException("Start position cannot be negative");
        }
        if (maxCount <= 0) {
            throw new TransactionServiceException("Max count must be positive");
        }

        List<Transaction> results = new ArrayList<>();
        long currentPosition = 0;
        
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath), StandardCharsets.UTF_8);
             CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                     .builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setIgnoreHeaderCase(true)
                     .setTrim(true)
                     .build())) {
            validateHeaders(csvParser.getHeaderNames());
            for (CSVRecord record : csvParser) {
                if (currentPosition < startPosition) {
                    currentPosition++;
                    continue;
                }
                
                try {
                    String recordAccountId = record.get("ACCOUNT_ID");
                    LocalDateTime recordDateTime = LocalDateTime.parse(record.get("DATE_TIME"));
                    
                    if (shouldIncludeRecord(recordAccountId, recordDateTime, accountId, fromDate, toDate)) {
                        Transaction transaction = CSVUtil.parseTransactionRecord(record, type);
                        results.add(transaction);
                        
                        if (results.size() >= maxCount) {
                            break;
                        }
                    }
                } catch (DateTimeParseException e) {
                    throw new TransactionServiceException("Invalid date format in CSV record: " + record.get("DATE_TIME"));
                } catch (NumberFormatException e) {
                    throw new TransactionServiceException("Invalid amount format in CSV record: " + record.get("AMOUNT"));
                }
                currentPosition++;
            }
        } catch (IOException e) {
            throw new TransactionServiceException("Error reading CSV file: " + filePath, e);
        }
        return results;
    }
    
    private static void validateHeaders(List<String> headers) {
        for (String requiredHeader : REQUIRED_HEADERS) {
            if (!headers.contains(requiredHeader)) {
                throw new TransactionServiceException("Missing required header in CSV file: " + requiredHeader);
            }
        }
    }
    
    private static boolean shouldIncludeRecord(String recordAccountId, LocalDateTime recordDateTime, String accountId, LocalDateTime fromDate, LocalDateTime toDate) {
        boolean accountMatches = accountId == null || accountId.isEmpty() || accountId.equals(recordAccountId);
        boolean dateInRange = (fromDate == null || !recordDateTime.isBefore(fromDate)) && (toDate == null || !recordDateTime.isAfter(toDate));
        
        return accountMatches && dateInRange;
    }
    
    public static boolean hasMoreTransactions(String filePath, long position) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return false;
        }

        int lineCount = 0;
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            while (reader.readLine() != null) {
                lineCount++;
            }
        }
        
        return position < (lineCount - 1);
    }
}
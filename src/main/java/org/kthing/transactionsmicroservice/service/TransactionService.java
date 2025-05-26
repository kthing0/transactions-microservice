package org.kthing.transactionsmicroservice.service;

import org.kthing.transactionsmicroservice.exception.TransactionServiceException;
import org.kthing.transactionsmicroservice.model.Transaction;
import org.kthing.transactionsmicroservice.model.TransactionPageResponse;
import org.kthing.transactionsmicroservice.util.PagedCsvReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class TransactionService {

    @Value("${transaction.incomes.file:incomes.csv}")
    private String incomesFilePath;

    @Value("${transaction.outcomes.file:outcomes.csv}")
    private String outcomesFilePath;

    private static final String PAGE_TOKEN_DELIMITER = ":";
    
    @Value("${transaction.page.size:20}")
    private int pageSize;

    public TransactionPageResponse getTransactions(String accountId, LocalDateTime fromDate, LocalDateTime toDate, String pageToken) {
        validateInput(accountId, fromDate, toDate, pageToken);
        long incomesPosition = 0;
        long outcomesPosition = 0;
        if (StringUtils.hasText(pageToken)) {
            String[] positions = pageToken.split(PAGE_TOKEN_DELIMITER);
            if (positions.length == 2) {
                try {
                    incomesPosition = Long.parseLong(positions[0]);
                    outcomesPosition = Long.parseLong(positions[1]);
                } catch (NumberFormatException e) {
                    throw new TransactionServiceException("Invalid page token format");
                }
            } else {
                throw new TransactionServiceException("Invalid page token format");
            }
        }

        List<Transaction> transactions = new ArrayList<>();
        long nextIncomesPosition = incomesPosition;
        long nextOutcomesPosition = outcomesPosition;
        boolean hasMore = false;

        try {
            List<Transaction> incomes = PagedCsvReader.readTransactions(incomesFilePath, Transaction.TransactionType.INCOME, incomesPosition, pageSize, accountId, fromDate, toDate);

            if (!incomes.isEmpty()) {
                transactions.addAll(incomes);
                nextIncomesPosition = incomesPosition + incomes.size();
            }

            List<Transaction> outcomes = PagedCsvReader.readTransactions( outcomesFilePath, Transaction.TransactionType.OUTCOME, outcomesPosition, pageSize, accountId, fromDate, toDate);

            if (!outcomes.isEmpty()) {
                transactions.addAll(outcomes);
                nextOutcomesPosition = outcomesPosition + outcomes.size();
            }

            transactions.sort(Comparator.comparing(Transaction::getDateTime));

            boolean moreIncomesExist = PagedCsvReader.hasMoreTransactions(incomesFilePath, nextIncomesPosition);
            boolean moreOutcomesExist = PagedCsvReader.hasMoreTransactions(outcomesFilePath, nextOutcomesPosition);
            
            if (transactions.size() > pageSize) {
                hasMore = true;
                transactions = transactions.subList(0, pageSize);
            } else {
                hasMore = moreIncomesExist || moreOutcomesExist;
            }

        } catch (IOException e) {
            throw new TransactionServiceException("Error reading transaction data", e);
        }

        String nextToken = hasMore ? nextIncomesPosition + PAGE_TOKEN_DELIMITER + nextOutcomesPosition : null;

        return TransactionPageResponse.builder()
                .transactions(transactions)
                .hasMore(hasMore)
                .nextPageToken(nextToken)
                .build();
    }
    private void validateInput(String accountId, LocalDateTime fromDate, LocalDateTime toDate, String pageToken) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new TransactionServiceException("fromDate cannot be after toDate");
        }
        
        if (StringUtils.hasText(accountId) && !accountId.matches("\\d+")) {
            throw new TransactionServiceException("Invalid account ID format");
        }
    }
}
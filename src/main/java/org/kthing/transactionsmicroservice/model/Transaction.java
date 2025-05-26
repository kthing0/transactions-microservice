package org.kthing.transactionsmicroservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    private String transactionId;
    private String customerId;
    private String accountId;
    private BigDecimal amount;
    private LocalDateTime dateTime;
    private TransactionType type;
    
    public enum TransactionType {
        INCOME, OUTCOME
    }
}
package org.kthing.transactionsmicroservice.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPageResponse {
    private List<Transaction> transactions;
    private boolean hasMore;
    private String nextPageToken;
}
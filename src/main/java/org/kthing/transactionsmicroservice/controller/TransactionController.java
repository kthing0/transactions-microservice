package org.kthing.transactionsmicroservice.controller;

import org.kthing.transactionsmicroservice.model.TransactionPageResponse;
import org.kthing.transactionsmicroservice.service.TransactionService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<TransactionPageResponse> getTransactions(
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(required = false) String pageToken) {
        
        TransactionPageResponse response = transactionService.getTransactions(accountId, fromDate, toDate, pageToken);
        return ResponseEntity.ok(response);
    }
}
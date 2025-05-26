package org.kthing.transactionsmicroservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kthing.transactionsmicroservice.model.Transaction;
import org.kthing.transactionsmicroservice.model.TransactionPageResponse;
import org.kthing.transactionsmicroservice.service.TransactionService;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    private MockMvc mockMvc;
    @Mock
    private TransactionService transactionService;
    @InjectMocks
    private TransactionController transactionController;
    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(transactionController).build();
    }

    @Test
    void testGetTransactions_NoFilters() throws Exception {
        List<Transaction> transactions = Arrays.asList(
                Transaction.builder()
                        .transactionId("123456789")
                        .customerId("10001")
                        .accountId("1000001")
                        .amount(new BigDecimal("500.00"))
                        .dateTime(LocalDateTime.parse("2023-01-01T10:00:00"))
                        .type(Transaction.TransactionType.INCOME)
                        .build(),
                Transaction.builder()
                        .transactionId("223456789")
                        .customerId("10001")
                        .accountId("1000001")
                        .amount(new BigDecimal("100.00"))
                        .dateTime(LocalDateTime.parse("2023-01-01T15:00:00"))
                        .type(Transaction.TransactionType.OUTCOME)
                        .build()
        );

        TransactionPageResponse mockResponse = TransactionPageResponse.builder()
                .transactions(transactions)
                .hasMore(false)
                .nextPageToken(null)
                .build();

        when(transactionService.getTransactions(eq(null), eq(null), eq(null), eq(null)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(2)))
                .andExpect(jsonPath("$.transactions[0].transactionId", is("123456789")))
                .andExpect(jsonPath("$.transactions[0].type", is("INCOME")))
                .andExpect(jsonPath("$.transactions[1].transactionId", is("223456789")))
                .andExpect(jsonPath("$.transactions[1].type", is("OUTCOME")))
                .andExpect(jsonPath("$.hasMore", is(false)))
                .andExpect(jsonPath("$.nextPageToken").doesNotExist());
    }

    @Test
    void testGetTransactions_WithFilters() throws Exception {
        List<Transaction> filteredTransactions = Arrays.asList(
                Transaction.builder()
                        .transactionId("123456789")
                        .customerId("10001")
                        .accountId("1000001")
                        .amount(new BigDecimal("500.00"))
                        .dateTime(LocalDateTime.parse("2023-01-01T10:00:00"))
                        .type(Transaction.TransactionType.INCOME)
                        .build()
        );

        TransactionPageResponse mockResponse = TransactionPageResponse.builder()
                .transactions(filteredTransactions)
                .hasMore(false)
                .nextPageToken(null)
                .build();

        LocalDateTime fromDate = LocalDateTime.parse("2023-01-01T00:00:00");
        LocalDateTime toDate = LocalDateTime.parse("2023-01-01T12:00:00");

        when(transactionService.getTransactions(eq("1000001"), eq(fromDate), eq(toDate), eq(null)))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/transactions")
                        .param("accountId", "1000001")
                        .param("fromDate", "2023-01-01T00:00:00")
                        .param("toDate", "2023-01-01T12:00:00")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.transactions[0].transactionId", is("123456789")))
                .andExpect(jsonPath("$.transactions[0].accountId", is("1000001")))
                .andExpect(jsonPath("$.hasMore", is(false)));
    }

    @Test
    void testGetTransactions_WithPagination() throws Exception {
        List<Transaction> transactions = Arrays.asList(
                Transaction.builder()
                        .transactionId("123456789")
                        .customerId("10001")
                        .accountId("1000001")
                        .amount(new BigDecimal("500.00"))
                        .dateTime(LocalDateTime.parse("2023-01-01T10:00:00"))
                        .type(Transaction.TransactionType.INCOME)
                        .build()
        );

        TransactionPageResponse mockResponse = TransactionPageResponse.builder()
                .transactions(transactions)
                .hasMore(true)
                .nextPageToken("10:5")
                .build();

        when(transactionService.getTransactions(eq(null), eq(null), eq(null), eq("5:3")))
                .thenReturn(mockResponse);

        mockMvc.perform(get("/api/transactions")
                        .param("pageToken", "5:3")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions", hasSize(1)))
                .andExpect(jsonPath("$.hasMore", is(true)))
                .andExpect(jsonPath("$.nextPageToken", is("10:5")));
    }
}
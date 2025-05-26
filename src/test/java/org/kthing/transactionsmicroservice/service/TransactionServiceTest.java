package org.kthing.transactionsmicroservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.kthing.transactionsmicroservice.model.Transaction;
import org.kthing.transactionsmicroservice.model.TransactionPageResponse;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;


@SpringBootTest
@TestExecutionListeners({
        DependencyInjectionTestExecutionListener.class
})
class TransactionServiceTest {

    @InjectMocks
    private TransactionService transactionService;

    @TempDir
    Path tempDir;

    private static final String TEST_INCOMES_CSV =
            "TRANSACTION_ID,CUSTOMER_ID,ACCOUNT_ID,AMOUNT,DATE_TIME\n" +
                    "123456789,10001,1000001,500.00,2023-01-01T10:00:00\n" +
                    "123456790,10002,1000002,750.50,2023-01-02T11:00:00\n" +
                    "123456791,10001,1000001,1200.75,2023-01-03T12:00:00\n";

    private static final String TEST_OUTCOMES_CSV =
            "TRANSACTION_ID,CUSTOMER_ID,ACCOUNT_ID,AMOUNT,DATE_TIME\n" +
                    "223456789,10001,1000001,100.00,2023-01-01T15:00:00\n" +
                    "223456790,10002,1000002,200.50,2023-01-02T16:00:00\n" +
                    "223456791,10001,1000001,300.75,2023-01-03T17:00:00\n";

    private File incomesFile;
    private File outcomesFile;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        
        incomesFile = tempDir.resolve("test-incomes.csv").toFile();
        outcomesFile = tempDir.resolve("test-outcomes.csv").toFile();

        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(incomesFile), StandardCharsets.UTF_8))) {
            writer.write(TEST_INCOMES_CSV);
        }
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(outcomesFile), StandardCharsets.UTF_8))) {
            writer.write(TEST_OUTCOMES_CSV);
        }
        ReflectionTestUtils.setField(transactionService, "incomesFilePath", incomesFile.getAbsolutePath(), String.class);
        ReflectionTestUtils.setField(transactionService, "outcomesFilePath", outcomesFile.getAbsolutePath(), String.class);
        ReflectionTestUtils.setField(transactionService, "pageSize", 20, int.class);
    }

    @Test
    void testGetTransactions_NoFilters() {
        TransactionPageResponse response = transactionService.getTransactions(null, null, null, null);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(6, response.getTransactions().size()),
            () -> assertFalse(response.isHasMore())
        );

        // тестим корректную сортировку транзакций по датам
        LocalDateTime prevDateTime = null;
        for (Transaction transaction : response.getTransactions()) {
            if (prevDateTime != null) {
                assertTrue(transaction.getDateTime().isAfter(prevDateTime) || 
                          transaction.getDateTime().isEqual(prevDateTime));
            }
            prevDateTime = transaction.getDateTime();
        }
    }

    @Test
    void testGetTransactions_FilterByAccountId() {
        String accountId = "1000001";
        TransactionPageResponse response = transactionService.getTransactions(accountId, null, null, null);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(4, response.getTransactions().size()),
            () -> assertTrue(response.isHasMore())
        );
        response.getTransactions().forEach(transaction -> 
            assertEquals(accountId, transaction.getAccountId())
        );
    }

    @Test
    void testGetTransactions_FilterByDateRange() {
        LocalDateTime fromDate = LocalDateTime.parse("2023-01-02T00:00:00");
        LocalDateTime toDate = LocalDateTime.parse("2023-01-02T23:59:59");

        TransactionPageResponse response = transactionService.getTransactions(null, fromDate, toDate, null);

        assertAll(
            () -> assertNotNull(response),
            () -> assertEquals(2, response.getTransactions().size()),
            () -> assertTrue(response.isHasMore())
        );
        response.getTransactions().forEach(transaction -> {
            assertTrue(transaction.getDateTime().isAfter(fromDate) || 
                      transaction.getDateTime().isEqual(fromDate));
            assertTrue(transaction.getDateTime().isBefore(toDate) || 
                      transaction.getDateTime().isEqual(toDate));
        });
    }
}
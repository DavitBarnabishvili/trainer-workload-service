package com.gym.crm.workloadservice.util;

import com.gym.crm.workload.util.TransactionContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TransactionContextTest {

    @AfterEach
    void tearDown() {
        TransactionContext.clear();
    }

    @Test
    void generateTransactionId_ShouldReturnNonNullUUID() {
        String transactionId = TransactionContext.generateTransactionId();

        assertNotNull(transactionId);
        assertTrue(transactionId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void setTransactionId_ShouldSetTransactionIdInMDC() {
        String transactionId = "test-transaction-123";

        TransactionContext.setTransactionId(transactionId);

        assertEquals(transactionId, TransactionContext.getTransactionId());
    }

    @Test
    void setTransactionId_WithNullValue_ShouldNotSetInMDC() {
        TransactionContext.setTransactionId(null);

        String result = TransactionContext.getTransactionId();
        assertNotNull(result);
    }

    @Test
    void setTransactionId_WithEmptyString_ShouldNotSetInMDC() {
        TransactionContext.setTransactionId("");

        String result = TransactionContext.getTransactionId();
        assertNotNull(result);
        assertNotEquals("", result);
    }

    @Test
    void getTransactionId_WhenNotSet_ShouldGenerateNew() {
        String transactionId = TransactionContext.getTransactionId();

        assertNotNull(transactionId);
        assertTrue(transactionId.matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"));
    }

    @Test
    void getTransactionId_WhenSet_ShouldReturnSameValue() {
        String expectedId = "test-123";
        TransactionContext.setTransactionId(expectedId);

        String actualId = TransactionContext.getTransactionId();

        assertEquals(expectedId, actualId);
    }

    @Test
    void clear_ShouldRemoveTransactionIdFromMDC() {
        TransactionContext.setTransactionId("test-123");

        TransactionContext.clear();

        String newId = TransactionContext.getTransactionId();
        assertNotEquals("test-123", newId);
    }

    @Test
    void startTransaction_ShouldGenerateAndSetNewTransactionId() {
        String transactionId = TransactionContext.startTransaction();

        assertNotNull(transactionId);
        assertEquals(transactionId, TransactionContext.getTransactionId());
    }

    @Test
    void startTransactionWithExistingId_ShouldUseProvidedId() {
        String existingId = "existing-transaction-456";

        String transactionId = TransactionContext.startTransaction(existingId);

        assertEquals(existingId, transactionId);
        assertEquals(existingId, TransactionContext.getTransactionId());
    }

    @Test
    void startTransactionWithNullId_ShouldGenerateNew() {
        String transactionId = TransactionContext.startTransaction(null);

        assertNotNull(transactionId);
        assertNotEquals("null", transactionId);
    }

    @Test
    void startTransactionWithEmptyId_ShouldGenerateNew() {
        String transactionId = TransactionContext.startTransaction("");

        assertNotNull(transactionId);
        assertNotEquals("", transactionId);
    }

    @Test
    void multipleTransactions_ShouldMaintainSeparateIds() {
        String firstId = TransactionContext.startTransaction();
        TransactionContext.clear();
        String secondId = TransactionContext.startTransaction();

        assertNotEquals(firstId, secondId);
    }
}

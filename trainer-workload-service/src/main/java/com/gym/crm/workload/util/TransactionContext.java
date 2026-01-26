package com.gym.crm.workload.util;

import org.slf4j.MDC;

import java.util.UUID;

public class TransactionContext {

    private static final String TRANSACTION_ID_KEY = "transactionId";

    public static String generateTransactionId() {
        return UUID.randomUUID().toString();
    }

    public static void setTransactionId(String transactionId) {
        if (transactionId != null && !transactionId.trim().isEmpty()) {
            MDC.put(TRANSACTION_ID_KEY, transactionId);
        }
    }

    public static String getTransactionId() {
        String transactionId = MDC.get(TRANSACTION_ID_KEY);
        if (transactionId == null || transactionId.trim().isEmpty()) {
            transactionId = generateTransactionId();
            setTransactionId(transactionId);
        }
        return transactionId;
    }

    public static void clear() {
        MDC.remove(TRANSACTION_ID_KEY);
    }

    public static String startTransaction() {
        String transactionId = generateTransactionId();
        setTransactionId(transactionId);
        return transactionId;
    }

    public static String startTransaction(String existingTransactionId) {
        if (existingTransactionId != null && !existingTransactionId.trim().isEmpty()) {
            setTransactionId(existingTransactionId);
            return existingTransactionId;
        }
        return startTransaction();
    }
}

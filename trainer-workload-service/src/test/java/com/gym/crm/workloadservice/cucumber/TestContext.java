package com.gym.crm.workloadservice.cucumber;

import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import lombok.Data;

@Data
public class TestContext {

    private TrainerWorkloadRequest lastRequest;
    private TrainerWorkloadResponse lastResponse;
    private Exception lastException;
    private String lastTransactionId;
    private int expectedStatusCode;

    public void reset() {
        lastRequest = null;
        lastResponse = null;
        lastException = null;
        lastTransactionId = null;
        expectedStatusCode = 0;
    }
}

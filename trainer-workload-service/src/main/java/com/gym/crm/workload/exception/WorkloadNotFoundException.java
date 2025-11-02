package com.gym.crm.workload.exception;

public class WorkloadNotFoundException extends RuntimeException {
    public WorkloadNotFoundException(String message) {
        super(message);
    }
}
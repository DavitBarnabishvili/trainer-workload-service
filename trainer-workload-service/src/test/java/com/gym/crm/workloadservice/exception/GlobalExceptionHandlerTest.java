package com.gym.crm.workloadservice.exception;

import com.gym.crm.workload.exception.GlobalExceptionHandler;
import com.gym.crm.workload.exception.ValidationException;
import com.gym.crm.workload.exception.WorkloadNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void handleWorkloadNotFoundException_ShouldReturnNotFound() {
        WorkloadNotFoundException exception = new WorkloadNotFoundException("Trainer not found");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleWorkloadNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Trainer not found", response.getBody().get("error"));
    }

    @Test
    void handleValidationException_ShouldReturnBadRequest() {
        ValidationException exception = new ValidationException("Validation failed");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().get("error"));
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError fieldError1 = new FieldError("trainerWorkloadRequest", "trainerUsername", "must not be blank");
        FieldError fieldError2 = new FieldError("trainerWorkloadRequest", "trainingDuration", "must be positive");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(fieldError1, fieldError2));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("must not be blank", response.getBody().get("trainerUsername"));
        assertEquals("must be positive", response.getBody().get("trainingDuration"));
    }

    @Test
    void handleConstraintViolationException_ShouldReturnBadRequest() {
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);

        when(violation.getPropertyPath()).thenReturn(mock(jakarta.validation.Path.class));
        when(violation.getPropertyPath().toString()).thenReturn("trainerUsername");
        when(violation.getMessage()).thenReturn("must not be blank");

        violations.add(violation);

        ConstraintViolationException exception = new ConstraintViolationException("Validation failed", violations);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleConstraintViolationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().containsKey("trainerUsername"));
    }

    @Test
    void handleIllegalArgumentException_ShouldReturnBadRequest() {
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleIllegalArgumentException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Invalid argument", response.getBody().get("error"));
    }

    @Test
    void handleGlobalException_ShouldReturnInternalServerError() {
        Exception exception = new RuntimeException("Unexpected error");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleGlobalException(exception);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().get("error").contains("Unexpected error"));
    }

    @Test
    void handleWorkloadNotFoundException_WithNullMessage_ShouldReturnNotFound() {
        WorkloadNotFoundException exception = new WorkloadNotFoundException(null);

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleWorkloadNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleValidationException_WithEmptyMessage_ShouldReturnBadRequest() {
        ValidationException exception = new ValidationException("");

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void handleMethodArgumentNotValidException_WithMultipleErrors_ShouldReturnAllErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);

        FieldError error1 = new FieldError("obj", "field1", "error1");
        FieldError error2 = new FieldError("obj", "field2", "error2");
        FieldError error3 = new FieldError("obj", "field3", "error3");

        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(error1, error2, error3));

        ResponseEntity<Map<String, String>> response = exceptionHandler.handleValidationExceptions(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(3, response.getBody().size());
    }
}

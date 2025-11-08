package com.gym.crm.workloadservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.crm.workload.TrainerWorkloadApplication;
import com.gym.crm.workload.controller.TrainerWorkloadController;
import com.gym.crm.workload.dto.ActionType;
import com.gym.crm.workload.dto.TrainerWorkloadRequest;
import com.gym.crm.workload.dto.TrainerWorkloadResponse;
import com.gym.crm.workload.exception.WorkloadNotFoundException;
import com.gym.crm.workload.security.JwtAuthenticationFilter;
import com.gym.crm.workload.security.JwtUtil;
import com.gym.crm.workload.service.TrainerWorkloadService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerWorkloadController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "jwt.secret=dGVzdC1zZWNyZXQta2V5LXRoYXQtaXMtYXQtbGVhc3QtMjU2LWJpdHMtbG9uZy1mb3ItdGVzdGluZw==",
        "jwt.expiration=3600000"
})
@ContextConfiguration(classes = TrainerWorkloadApplication.class)
public class TrainerWorkloadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainerWorkloadService workloadService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    private TrainerWorkloadRequest request;
    private TrainerWorkloadResponse response;

    @BeforeEach
    void setUp() {
        request = TrainerWorkloadRequest.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .trainingDate(LocalDate.of(2025, 11, 15))
                .trainingDuration(60)
                .actionType(ActionType.ADD)
                .build();

        response = TrainerWorkloadResponse.builder()
                .trainerUsername("john.doe")
                .trainerFirstName("John")
                .trainerLastName("Doe")
                .isActive(true)
                .years(new ArrayList<>())
                .build();
    }

    @Test
    void testUpdateTrainerWorkload_Success() throws Exception {
        doNothing().when(workloadService).updateWorkload(any(TrainerWorkloadRequest.class));

        mockMvc.perform(post("/api/trainer/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(workloadService).updateWorkload(any(TrainerWorkloadRequest.class));
    }

    @Test
    void testUpdateTrainerWorkload_InvalidRequest() throws Exception {
        request.setTrainerUsername(null);

        mockMvc.perform(post("/api/trainer/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(workloadService, never()).updateWorkload(any());
    }

    @Test
    void testGetTrainerWorkload_Success() throws Exception {
        when(workloadService.getTrainerWorkload("john.doe")).thenReturn(response);

        mockMvc.perform(get("/api/trainer/workload/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trainerUsername").value("john.doe"))
                .andExpect(jsonPath("$.trainerFirstName").value("John"));

        verify(workloadService).getTrainerWorkload("john.doe");
    }

    @Test
    void testGetTrainerWorkload_NotFound() throws Exception {
        when(workloadService.getTrainerWorkload("unknown"))
                .thenThrow(new WorkloadNotFoundException("Trainer workload not found"));

        mockMvc.perform(get("/api/trainer/workload/unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateTrainerWorkload_NegativeDuration() throws Exception {
        request.setTrainingDuration(-10);

        mockMvc.perform(post("/api/trainer/workload")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
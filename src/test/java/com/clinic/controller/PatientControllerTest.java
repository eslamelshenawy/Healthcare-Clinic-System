package com.clinic.controller;

import com.clinic.config.SecurityConfig;
import com.clinic.dto.request.PatientRegistrationRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.security.CustomUserDetailsService;
import com.clinic.security.JwtService;
import com.clinic.security.TokenBlacklist;
import com.clinic.service.PatientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@Import(SecurityConfig.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PatientService patientService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    @MockitoBean
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/patients should return 201 when authenticated")
    @WithMockUser(roles = "ADMIN")
    void registerPatient_Success() throws Exception {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                "John Doe", "جون دو", "john@example.com",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital"
        );

        PatientResponse response = new PatientResponse(
                1L, "John Doe", "جون دو", "john@example.com",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital",
                LocalDateTime.now()
        );

        when(patientService.registerPatient(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    @DisplayName("POST /api/v1/patients should return 403 when not authenticated")
    void registerPatient_Unauthorized() throws Exception {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                "John Doe", "جون دو", "john@example.com",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital"
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /api/v1/patients should return 400 for invalid email")
    @WithMockUser(roles = "ADMIN")
    void registerPatient_InvalidEmail() throws Exception {
        PatientRegistrationRequest request = new PatientRegistrationRequest(
                "John Doe", "جون دو", "invalid-email",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital"
        );

        mockMvc.perform(post("/api/v1/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/patients/{id} should return 200")
    @WithMockUser(roles = "ADMIN")
    void getPatient_Success() throws Exception {
        PatientResponse response = new PatientResponse(
                1L, "John Doe", "جون دو", "john@example.com",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital",
                LocalDateTime.now()
        );

        when(patientService.getPatientById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("DELETE /api/v1/patients/{id} should return 204")
    @WithMockUser(roles = "ADMIN")
    void deletePatient_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/patients/1"))
                .andExpect(status().isNoContent());
    }
}

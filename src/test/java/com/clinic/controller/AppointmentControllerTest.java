package com.clinic.controller;

import com.clinic.config.SecurityConfig;
import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.security.CustomUserDetailsService;
import com.clinic.security.JwtService;
import com.clinic.security.TokenBlacklist;
import com.clinic.service.AppointmentService;
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

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AppointmentController.class)
@Import(SecurityConfig.class)
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AppointmentService appointmentService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    @MockitoBean
    private TokenBlacklist tokenBlacklist;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /api/v1/appointments should return 201 when authenticated")
    @WithMockUser(roles = "ADMIN")
    void scheduleAppointment_Success() throws Exception {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        AppointmentRequest request = new AppointmentRequest(
                1L, 1L, futureDate, "Checkup");

        AppointmentResponse response = new AppointmentResponse(
                1L, 1L, "John", 1L, "Dr. Ahmed", futureDate,
                "SCHEDULED", "Checkup", LocalDateTime.now());

        when(appointmentService.scheduleAppointment(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("SCHEDULED"));
    }

    @Test
    @DisplayName("POST /api/v1/appointments should return 403 without auth")
    void scheduleAppointment_Unauthorized() throws Exception {
        LocalDateTime futureDate = LocalDateTime.now().plusDays(1);
        AppointmentRequest request = new AppointmentRequest(
                1L, 1L, futureDate, "Checkup");

        mockMvc.perform(post("/api/v1/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/appointments/{id} should return 200")
    @WithMockUser(roles = "ADMIN")
    void getAppointment_Success() throws Exception {
        AppointmentResponse response = new AppointmentResponse(
                1L, 1L, "John", 1L, "Dr. Ahmed",
                LocalDateTime.now().plusDays(1),
                "SCHEDULED", "Checkup", LocalDateTime.now());

        when(appointmentService.getAppointmentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/appointments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
}

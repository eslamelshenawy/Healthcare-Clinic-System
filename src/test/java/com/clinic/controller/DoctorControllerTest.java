package com.clinic.controller;

import com.clinic.config.SecurityConfig;
import com.clinic.dto.response.DoctorResponse;
import com.clinic.security.CustomUserDetailsService;
import com.clinic.security.JwtService;
import com.clinic.security.TokenBlacklist;
import com.clinic.service.DoctorService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorController.class)
@Import(SecurityConfig.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DoctorService doctorService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private CustomUserDetailsService userDetailsService;
    @MockitoBean
    private TokenBlacklist tokenBlacklist;

    @Test
    @DisplayName("GET /api/v1/doctors should return 200 without auth")
    void getAllDoctors_NoAuthRequired() throws Exception {
        List<DoctorResponse> doctors = List.of(
                new DoctorResponse(1L, "Dr. Ahmed", "د. أحمد",
                        "CARDIOLOGY", 15, 30));

        when(doctorService.getAllDoctors()).thenReturn(doctors);

        mockMvc.perform(get("/api/v1/doctors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nameEn").value("Dr. Ahmed"));
    }

    @Test
    @DisplayName("GET /api/v1/doctors/{id} should return 200 without auth")
    void getDoctorById_NoAuthRequired() throws Exception {
        DoctorResponse doctor = new DoctorResponse(
                1L, "Dr. Ahmed", "د. أحمد", "CARDIOLOGY", 15, 30);

        when(doctorService.getDoctorById(1L)).thenReturn(doctor);

        mockMvc.perform(get("/api/v1/doctors/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/doctors/search should return 200")
    void searchDoctors() throws Exception {
        List<DoctorResponse> doctors = List.of(
                new DoctorResponse(1L, "Dr. Ahmed", "د. أحمد",
                        "CARDIOLOGY", 15, 30));

        when(doctorService.searchDoctors("Ahmed")).thenReturn(doctors);

        mockMvc.perform(get("/api/v1/doctors/search").param("name", "Ahmed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nameEn").value("Dr. Ahmed"));
    }
}

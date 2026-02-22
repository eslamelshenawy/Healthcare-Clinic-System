package com.clinic.service;

import com.clinic.dto.request.PatientRegistrationRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.entity.Address;
import com.clinic.entity.Patient;
import com.clinic.exception.DuplicateResourceException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.PatientMapper;
import com.clinic.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientMapper patientMapper;

    @InjectMocks
    private PatientService patientService;

    private PatientRegistrationRequest validRequest;
    private Patient patient;
    private PatientResponse patientResponse;

    @BeforeEach
    void setUp() {
        validRequest = new PatientRegistrationRequest(
                "John Doe", "جون دو", "john@example.com",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital"
        );

        patient = Patient.builder()
                .id(1L)
                .fullNameEn("John Doe")
                .fullNameAr("جون دو")
                .email("john@example.com")
                .mobileNumber("+96512345678")
                .dateOfBirth(LocalDate.of(1990, 5, 15))
                .nationalId("123456789")
                .address(new Address("123 Main St", "Kuwait City", "Capital"))
                .deleted(false)
                .build();

        patientResponse = new PatientResponse(
                1L, "John Doe", "جون دو", "john@example.com",
                "+96512345678", LocalDate.of(1990, 5, 15),
                "123456789", "123 Main St", "Kuwait City", "Capital",
                LocalDateTime.now()
        );
    }

    @Test
    @DisplayName("Should register patient successfully")
    void registerPatient_Success() {
        when(patientRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(patientRepository.existsByNationalId("123456789")).thenReturn(false);
        when(patientMapper.toEntity(validRequest)).thenReturn(patient);
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        PatientResponse result = patientService.registerPatient(validRequest);

        assertNotNull(result);
        assertEquals("john@example.com", result.email());
        verify(patientRepository).save(any(Patient.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate email")
    void registerPatient_DuplicateEmail() {
        when(patientRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> patientService.registerPatient(validRequest));

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException for duplicate national ID")
    void registerPatient_DuplicateNationalId() {
        when(patientRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(patientRepository.existsByNationalId("123456789")).thenReturn(true);

        assertThrows(DuplicateResourceException.class,
                () -> patientService.registerPatient(validRequest));

        verify(patientRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should get patient by ID successfully")
    void getPatientById_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientMapper.toResponse(patient)).thenReturn(patientResponse);

        PatientResponse result = patientService.getPatientById(1L);

        assertNotNull(result);
        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent patient")
    void getPatientById_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.getPatientById(99L));
    }

    @Test
    @DisplayName("Should soft delete patient")
    void softDeletePatient_Success() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        patientService.softDeletePatient(1L);

        assertTrue(patient.isDeleted());
        verify(patientRepository).save(patient);
    }

    @Test
    @DisplayName("Should throw when soft deleting non-existent patient")
    void softDeletePatient_NotFound() {
        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> patientService.softDeletePatient(99L));
    }
}

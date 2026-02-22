package com.clinic.service;

import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.Doctor;
import com.clinic.entity.enums.Specialty;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DoctorMapper;
import com.clinic.repository.DoctorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private DoctorMapper doctorMapper;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    @DisplayName("Should return all doctors")
    void getAllDoctors() {
        List<Doctor> doctors = List.of(
                Doctor.builder().id(1L).nameEn("Dr. Ahmed")
                        .specialty(Specialty.CARDIOLOGY).build());
        List<DoctorResponse> expected = List.of(
                new DoctorResponse(1L, "Dr. Ahmed", "د. أحمد",
                        "CARDIOLOGY", 15, 30));

        when(doctorRepository.findAll()).thenReturn(doctors);
        when(doctorMapper.toResponseList(doctors)).thenReturn(expected);

        List<DoctorResponse> result = doctorService.getAllDoctors();

        assertEquals(1, result.size());
        assertEquals("Dr. Ahmed", result.get(0).nameEn());
    }

    @Test
    @DisplayName("Should return doctor by ID")
    void getDoctorById_Success() {
        Doctor doctor = Doctor.builder().id(1L).nameEn("Dr. Ahmed")
                .specialty(Specialty.CARDIOLOGY).build();
        DoctorResponse expected = new DoctorResponse(
                1L, "Dr. Ahmed", "د. أحمد", "CARDIOLOGY", 15, 30);

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(doctorMapper.toResponse(doctor)).thenReturn(expected);

        DoctorResponse result = doctorService.getDoctorById(1L);

        assertEquals(1L, result.id());
    }

    @Test
    @DisplayName("Should throw when doctor not found")
    void getDoctorById_NotFound() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.getDoctorById(99L));
    }

    @Test
    @DisplayName("Should return doctors by specialty")
    void getDoctorsBySpecialty() {
        List<Doctor> doctors = List.of(
                Doctor.builder().id(1L).specialty(Specialty.CARDIOLOGY).build());
        List<DoctorResponse> expected = List.of(
                new DoctorResponse(1L, "Dr. Ahmed", "د. أحمد",
                        "CARDIOLOGY", 15, 30));

        when(doctorRepository.findBySpecialty(Specialty.CARDIOLOGY)).thenReturn(doctors);
        when(doctorMapper.toResponseList(doctors)).thenReturn(expected);

        List<DoctorResponse> result = doctorService
                .getDoctorsBySpecialty(Specialty.CARDIOLOGY);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should search doctors by name")
    void searchDoctors() {
        List<Doctor> doctors = List.of(
                Doctor.builder().id(1L).nameEn("Dr. Ahmed").build());
        List<DoctorResponse> expected = List.of(
                new DoctorResponse(1L, "Dr. Ahmed", "د. أحمد",
                        "CARDIOLOGY", 15, 30));

        when(doctorRepository.searchByName("Ahmed")).thenReturn(doctors);
        when(doctorMapper.toResponseList(doctors)).thenReturn(expected);

        List<DoctorResponse> result = doctorService.searchDoctors("Ahmed");

        assertEquals(1, result.size());
    }
}

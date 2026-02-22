package com.clinic.service;

import com.clinic.dto.request.AppointmentRequest;
import com.clinic.dto.request.AppointmentUpdateRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.Appointment;
import com.clinic.entity.Doctor;
import com.clinic.entity.Patient;
import com.clinic.entity.enums.AppointmentStatus;
import com.clinic.exception.AppointmentConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.AppointmentMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;
    @Mock
    private PatientRepository patientRepository;
    @Mock
    private DoctorRepository doctorRepository;
    @Mock
    private AppointmentMapper appointmentMapper;
    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private AppointmentService appointmentService;

    private Patient patient;
    private Doctor doctor;
    private LocalDateTime futureDateTime;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .id(1L).fullNameEn("John").email("john@test.com").build();
        doctor = Doctor.builder()
                .id(1L).nameEn("Dr. Ahmed").consultationDurationMinutes(30).build();
        futureDateTime = LocalDateTime.now().plusDays(1);
    }

    @Test
    @DisplayName("Should schedule appointment when no conflict exists")
    void scheduleAppointment_NoConflict() {
        AppointmentRequest request = new AppointmentRequest(
                1L, 1L, futureDateTime, "Checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());

        Appointment saved = Appointment.builder()
                .id(1L).patient(patient).doctor(doctor)
                .appointmentDateTime(futureDateTime)
                .status(AppointmentStatus.SCHEDULED).build();
        when(appointmentRepository.save(any())).thenReturn(saved);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                1L, 1L, "John", 1L, "Dr. Ahmed", futureDateTime,
                "SCHEDULED", "Checkup", LocalDateTime.now());
        when(appointmentMapper.toResponse(saved)).thenReturn(expectedResponse);

        AppointmentResponse result = appointmentService.scheduleAppointment(request);

        assertNotNull(result);
        assertEquals(1L, result.id());
        verify(notificationService).sendAppointmentConfirmation(
                any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should throw conflict when doctor time slot is taken")
    void scheduleAppointment_Conflict() {
        AppointmentRequest request = new AppointmentRequest(
                1L, 1L, futureDateTime, "Checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithLock(1L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any()))
                .thenReturn(List.of(Appointment.builder().id(99L).build()));

        assertThrows(AppointmentConflictException.class,
                () -> appointmentService.scheduleAppointment(request));
    }

    @Test
    @DisplayName("Should throw when patient not found")
    void scheduleAppointment_PatientNotFound() {
        AppointmentRequest request = new AppointmentRequest(
                99L, 1L, futureDateTime, "Checkup");

        when(patientRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.scheduleAppointment(request));
    }

    @Test
    @DisplayName("Should throw when doctor not found")
    void scheduleAppointment_DoctorNotFound() {
        AppointmentRequest request = new AppointmentRequest(
                1L, 99L, futureDateTime, "Checkup");

        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findByIdWithLock(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.scheduleAppointment(request));
    }

    @Test
    @DisplayName("Should update appointment successfully")
    void updateAppointment_Success() {
        LocalDateTime newDateTime = futureDateTime.plusHours(2);
        AppointmentUpdateRequest updateRequest = new AppointmentUpdateRequest(
                newDateTime, AppointmentStatus.CONFIRMED, "Updated reason");

        Appointment existing = Appointment.builder()
                .id(1L).patient(patient).doctor(doctor)
                .appointmentDateTime(futureDateTime)
                .status(AppointmentStatus.SCHEDULED)
                .reason("Original").build();

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(appointmentRepository.findConflictingAppointments(eq(1L), any(), any()))
                .thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any())).thenReturn(existing);

        AppointmentResponse expectedResponse = new AppointmentResponse(
                1L, 1L, "John", 1L, "Dr. Ahmed", newDateTime,
                "CONFIRMED", "Updated reason", LocalDateTime.now());
        when(appointmentMapper.toResponse(any())).thenReturn(expectedResponse);

        AppointmentResponse result = appointmentService.updateAppointment(1L, updateRequest);

        assertNotNull(result);
        assertEquals("CONFIRMED", result.status());
    }

    @Test
    @DisplayName("Should throw when updating non-existent appointment")
    void updateAppointment_NotFound() {
        AppointmentUpdateRequest request = new AppointmentUpdateRequest(
                null, AppointmentStatus.CANCELLED, null);

        when(appointmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> appointmentService.updateAppointment(99L, request));
    }

    @Test
    @DisplayName("Should get appointment by ID")
    void getAppointmentById_Success() {
        Appointment appointment = Appointment.builder()
                .id(1L).patient(patient).doctor(doctor)
                .appointmentDateTime(futureDateTime)
                .status(AppointmentStatus.SCHEDULED).build();

        AppointmentResponse expected = new AppointmentResponse(
                1L, 1L, "John", 1L, "Dr. Ahmed", futureDateTime,
                "SCHEDULED", null, LocalDateTime.now());

        when(appointmentRepository.findById(1L)).thenReturn(Optional.of(appointment));
        when(appointmentMapper.toResponse(appointment)).thenReturn(expected);

        AppointmentResponse result = appointmentService.getAppointmentById(1L);

        assertEquals(1L, result.id());
    }
}

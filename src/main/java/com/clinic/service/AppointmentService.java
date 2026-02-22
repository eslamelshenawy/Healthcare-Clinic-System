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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentService {

    private static final Logger log = LoggerFactory.getLogger(AppointmentService.class);

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentMapper appointmentMapper;
    private final NotificationService notificationService;

    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
    public AppointmentResponse scheduleAppointment(AppointmentRequest request) {
        log.info("Scheduling appointment: patient={}, doctor={}, dateTime={}",
                request.patientId(), request.doctorId(), request.appointmentDateTime());

        Patient patient = patientRepository.findById(request.patientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Patient", "id", request.patientId()));

        Doctor doctor = doctorRepository.findById(request.doctorId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Doctor", "id", request.doctorId()));

        checkForConflicts(doctor, request.appointmentDateTime(), null);

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDateTime(request.appointmentDateTime())
                .status(AppointmentStatus.SCHEDULED)
                .reason(request.reason())
                .build();

        Appointment saved = appointmentRepository.save(appointment);

        notificationService.sendAppointmentConfirmation(
                patient.getEmail(), patient.getFullNameEn(),
                doctor.getNameEn(), saved.getAppointmentDateTime());

        log.info("Appointment scheduled with ID: {}", saved.getId());
        return appointmentMapper.toResponse(saved);
    }

    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
    public AppointmentResponse updateAppointment(Long id, AppointmentUpdateRequest request) {
        log.info("Updating appointment ID: {}", id);

        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));

        if (request.appointmentDateTime() != null) {
            checkForConflicts(appointment.getDoctor(), request.appointmentDateTime(), id);
            appointment.setAppointmentDateTime(request.appointmentDateTime());
        }

        if (request.status() != null) {
            appointment.setStatus(request.status());
        }

        if (request.reason() != null) {
            appointment.setReason(request.reason());
        }

        Appointment updated = appointmentRepository.save(appointment);
        log.info("Appointment {} updated successfully", id);
        return appointmentMapper.toResponse(updated);
    }

    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment", "id", id));
        return appointmentMapper.toResponse(appointment);
    }

    public List<AppointmentResponse> getAppointmentsByPatient(Long patientId) {
        return appointmentMapper.toResponseList(
                appointmentRepository.findByPatientId(patientId));
    }

    private void checkForConflicts(Doctor doctor, LocalDateTime dateTime, Long excludeId) {
        LocalDateTime start = dateTime;
        LocalDateTime end = start.plusMinutes(doctor.getConsultationDurationMinutes());

        List<Appointment> conflicts = appointmentRepository
                .findConflictingAppointments(doctor.getId(), start, end);

        if (excludeId != null) {
            conflicts.removeIf(a -> a.getId().equals(excludeId));
        }

        if (!conflicts.isEmpty()) {
            throw new AppointmentConflictException(
                    "Doctor " + doctor.getNameEn() +
                            " already has an appointment that overlaps with the requested time slot.");
        }
    }
}

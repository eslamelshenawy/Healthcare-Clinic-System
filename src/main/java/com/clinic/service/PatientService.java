package com.clinic.service;

import com.clinic.dto.request.PatientRegistrationRequest;
import com.clinic.dto.response.PagedResponse;
import com.clinic.dto.response.PatientResponse;
import com.clinic.dto.response.PatientWithAppointmentsResponse;
import com.clinic.entity.Patient;
import com.clinic.exception.DuplicateResourceException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.PatientMapper;
import com.clinic.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PatientService {

    private static final Logger log = LoggerFactory.getLogger(PatientService.class);

    private final PatientRepository patientRepository;
    private final PatientMapper patientMapper;

    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
    public PatientResponse registerPatient(PatientRegistrationRequest request) {
        log.info("Registering patient with email: {}", request.email());

        if (patientRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Patient", "email", request.email());
        }
        if (patientRepository.existsByNationalId(request.nationalId())) {
            throw new DuplicateResourceException("Patient", "nationalId", request.nationalId());
        }

        Patient patient = patientMapper.toEntity(request);
        Patient saved = patientRepository.save(patient);

        log.info("Patient registered successfully with ID: {}", saved.getId());
        return patientMapper.toResponse(saved);
    }

    public PatientResponse getPatientById(Long id) {
        Patient patient = findPatientOrThrow(id);
        return patientMapper.toResponse(patient);
    }

    @Cacheable(value = "patients", key = "'all-page-' + #pageable.pageNumber")
    public PagedResponse<PatientWithAppointmentsResponse> getAllPatientsWithAppointments(
            Pageable pageable) {
        log.debug("Fetching all patients with appointments, page: {}", pageable.getPageNumber());

        Page<Patient> patientPage = patientRepository.findAll(pageable);
        Page<PatientWithAppointmentsResponse> responsePage =
                patientPage.map(patientMapper::toResponseWithAppointments);

        return PagedResponse.from(responsePage);
    }

    @Transactional
    @CacheEvict(value = "patients", allEntries = true)
    public void softDeletePatient(Long id) {
        log.info("Soft deleting patient with ID: {}", id);

        Patient patient = findPatientOrThrow(id);
        patient.setDeleted(true);
        patientRepository.save(patient);

        log.info("Patient {} soft-deleted successfully", id);
    }

    private Patient findPatientOrThrow(Long id) {
        return patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient", "id", id));
    }
}

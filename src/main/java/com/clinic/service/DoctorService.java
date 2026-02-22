package com.clinic.service;

import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.enums.Specialty;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DoctorMapper;
import com.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorService {

    private static final Logger log = LoggerFactory.getLogger(DoctorService.class);

    private final DoctorRepository doctorRepository;
    private final DoctorMapper doctorMapper;

    @Cacheable(value = "doctors")
    public List<DoctorResponse> getAllDoctors() {
        log.debug("Fetching all doctors");
        return doctorMapper.toResponseList(doctorRepository.findAll());
    }

    @Cacheable(value = "doctors", key = "#id")
    public DoctorResponse getDoctorById(Long id) {
        return doctorMapper.toResponse(
                doctorRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Doctor", "id", id)));
    }

    @Cacheable(value = "doctors", key = "'specialty-' + #specialty")
    public List<DoctorResponse> getDoctorsBySpecialty(Specialty specialty) {
        log.debug("Fetching doctors by specialty: {}", specialty);
        return doctorMapper.toResponseList(doctorRepository.findBySpecialty(specialty));
    }

    public List<DoctorResponse> searchDoctors(String name) {
        return doctorMapper.toResponseList(doctorRepository.searchByName(name));
    }
}

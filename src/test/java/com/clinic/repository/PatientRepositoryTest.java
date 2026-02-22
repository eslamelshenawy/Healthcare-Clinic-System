package com.clinic.repository;

import com.clinic.entity.Address;
import com.clinic.entity.Patient;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Patient createPatient(String email, String nationalId, boolean deleted) {
        return Patient.builder()
                .fullNameEn("Test User").fullNameAr("مستخدم تجريبي")
                .email(email).mobileNumber("+96500000000")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationalId(nationalId)
                .address(new Address("Street", "City", "Region"))
                .deleted(deleted).build();
    }

    @Test
    @DisplayName("Should find patient by email")
    void findByEmail() {
        patientRepository.save(createPatient("test@test.com", "TEST123", false));

        Optional<Patient> found = patientRepository.findByEmail("test@test.com");

        assertTrue(found.isPresent());
        assertEquals("Test User", found.get().getFullNameEn());
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail() {
        patientRepository.save(createPatient("exists@test.com", "EX123", false));

        assertTrue(patientRepository.existsByEmail("exists@test.com"));
        assertFalse(patientRepository.existsByEmail("notexists@test.com"));
    }

    @Test
    @DisplayName("Should return true when national ID exists")
    void existsByNationalId() {
        patientRepository.save(createPatient("nat@test.com", "NAT123", false));

        assertTrue(patientRepository.existsByNationalId("NAT123"));
        assertFalse(patientRepository.existsByNationalId("NAT999"));
    }

    @Test
    @DisplayName("Soft-deleted patient should not appear in findById")
    void softDeletedPatient_NotFoundInFindById() {
        Patient saved = patientRepository.save(
                createPatient("deleted@test.com", "DEL123", true));

        // Flush and clear persistence context so the next query hits the DB
        entityManager.flush();
        entityManager.clear();

        Optional<Patient> found = patientRepository.findById(saved.getId());

        assertTrue(found.isEmpty());
    }

    @Test
    @DisplayName("Soft-deleted patient should appear in findByIdIncludingDeleted")
    void softDeletedPatient_FoundInIncludingDeleted() {
        Patient saved = patientRepository.save(
                createPatient("deleted2@test.com", "DEL456", true));

        Optional<Patient> found = patientRepository
                .findByIdIncludingDeleted(saved.getId());

        assertTrue(found.isPresent());
        assertTrue(found.get().isDeleted());
    }
}

package com.clinic.repository;

import com.clinic.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByEmail(String email);

    boolean existsByNationalId(String nationalId);

    Optional<Patient> findByEmail(String email);

    @Query(value = "SELECT * FROM patients WHERE id = :id", nativeQuery = true)
    Optional<Patient> findByIdIncludingDeleted(@Param("id") Long id);

    Page<Patient> findAll(Pageable pageable);
}

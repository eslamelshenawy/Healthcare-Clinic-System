package com.clinic.repository;

import com.clinic.entity.Doctor;
import com.clinic.entity.enums.Specialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findBySpecialty(Specialty specialty);

    @Query("SELECT d FROM Doctor d WHERE d.nameEn LIKE %:name% OR d.nameAr LIKE %:name%")
    List<Doctor> searchByName(@Param("name") String name);
}

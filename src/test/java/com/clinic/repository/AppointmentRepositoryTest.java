package com.clinic.repository;

import com.clinic.entity.Address;
import com.clinic.entity.Appointment;
import com.clinic.entity.Doctor;
import com.clinic.entity.Patient;
import com.clinic.entity.enums.AppointmentStatus;
import com.clinic.entity.enums.Specialty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patient = Patient.builder()
                .fullNameEn("Test Patient").fullNameAr("مريض تجريبي")
                .email("patient@test.com").mobileNumber("+96500000000")
                .dateOfBirth(LocalDate.of(1990, 1, 1))
                .nationalId("PAT123")
                .address(new Address("Street", "City", "Region"))
                .deleted(false).build();
        entityManager.persist(patient);

        doctor = Doctor.builder()
                .nameEn("Dr. Test").nameAr("د. تجريبي")
                .specialty(Specialty.GENERAL_PRACTICE)
                .yearsOfExperience(10)
                .consultationDurationMinutes(30).build();
        entityManager.persist(doctor);

        entityManager.flush();
    }

    @Test
    @DisplayName("Should find appointments by patient ID")
    void findByPatientId() {
        Appointment appointment = Appointment.builder()
                .patient(patient).doctor(doctor)
                .appointmentDateTime(LocalDateTime.now().plusDays(1))
                .status(AppointmentStatus.SCHEDULED)
                .reason("Checkup").build();
        entityManager.persist(appointment);
        entityManager.flush();

        List<Appointment> found = appointmentRepository
                .findByPatientId(patient.getId());

        assertEquals(1, found.size());
    }

    @Test
    @DisplayName("Should find conflicting appointments")
    void findConflictingAppointments() {
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(1)
                .withHour(10).withMinute(0);

        Appointment existing = Appointment.builder()
                .patient(patient).doctor(doctor)
                .appointmentDateTime(appointmentTime)
                .status(AppointmentStatus.SCHEDULED).build();
        entityManager.persist(existing);
        entityManager.flush();

        List<Appointment> conflicts = appointmentRepository
                .findConflictingAppointments(
                        doctor.getId(),
                        appointmentTime.minusMinutes(15),
                        appointmentTime.plusMinutes(15));

        assertFalse(conflicts.isEmpty());
    }

    @Test
    @DisplayName("Should not find cancelled appointments as conflicts")
    void findConflictingAppointments_IgnoresCancelled() {
        LocalDateTime appointmentTime = LocalDateTime.now().plusDays(2)
                .withHour(14).withMinute(0);

        Appointment cancelled = Appointment.builder()
                .patient(patient).doctor(doctor)
                .appointmentDateTime(appointmentTime)
                .status(AppointmentStatus.CANCELLED).build();
        entityManager.persist(cancelled);
        entityManager.flush();

        List<Appointment> conflicts = appointmentRepository
                .findConflictingAppointments(
                        doctor.getId(),
                        appointmentTime.minusMinutes(15),
                        appointmentTime.plusMinutes(15));

        assertTrue(conflicts.isEmpty());
    }
}

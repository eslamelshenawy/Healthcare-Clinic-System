package com.clinic.init;

import com.clinic.entity.AppUser;
import com.clinic.entity.Doctor;
import com.clinic.entity.enums.Role;
import com.clinic.entity.enums.Specialty;
import com.clinic.repository.AppUserRepository;
import com.clinic.repository.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final DoctorRepository doctorRepository;
    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedDoctors();
        seedUsers();
    }

    private void seedDoctors() {
        if (doctorRepository.count() > 0) return;

        List<Doctor> doctors = List.of(
                Doctor.builder()
                        .nameEn("Dr. Ahmed Hassan").nameAr("د. أحمد حسن")
                        .specialty(Specialty.CARDIOLOGY)
                        .yearsOfExperience(15)
                        .consultationDurationMinutes(30).build(),
                Doctor.builder()
                        .nameEn("Dr. Fatima Al-Rashid").nameAr("د. فاطمة الراشد")
                        .specialty(Specialty.DERMATOLOGY)
                        .yearsOfExperience(10)
                        .consultationDurationMinutes(20).build(),
                Doctor.builder()
                        .nameEn("Dr. Omar Khalil").nameAr("د. عمر خليل")
                        .specialty(Specialty.PEDIATRICS)
                        .yearsOfExperience(12)
                        .consultationDurationMinutes(25).build(),
                Doctor.builder()
                        .nameEn("Dr. Sara Nouri").nameAr("د. سارة نوري")
                        .specialty(Specialty.GENERAL_PRACTICE)
                        .yearsOfExperience(8)
                        .consultationDurationMinutes(15).build(),
                Doctor.builder()
                        .nameEn("Dr. Khalid Al-Mansour").nameAr("د. خالد المنصور")
                        .specialty(Specialty.ORTHOPEDICS)
                        .yearsOfExperience(20)
                        .consultationDurationMinutes(30).build()
        );

        doctorRepository.saveAll(doctors);
        log.info("Seeded {} doctors", doctors.size());
    }

    private void seedUsers() {
        if (appUserRepository.count() > 0) return;

        AppUser admin = AppUser.builder()
                .username("admin")
                .password(passwordEncoder.encode("admin123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();

        AppUser receptionist = AppUser.builder()
                .username("receptionist")
                .password(passwordEncoder.encode("recep123"))
                .role(Role.RECEPTIONIST)
                .enabled(true)
                .build();

        appUserRepository.saveAll(List.of(admin, receptionist));
        log.info("Seeded default users: admin, receptionist");
    }
}

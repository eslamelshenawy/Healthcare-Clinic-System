package com.clinic.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record PatientResponse(
        Long id,
        String fullNameEn,
        String fullNameAr,
        String email,
        String mobileNumber,
        LocalDate dateOfBirth,
        String nationalId,
        String street,
        String city,
        String region,
        LocalDateTime createdAt
) {
}

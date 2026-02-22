package com.clinic.dto.response;

public record DoctorResponse(
        Long id,
        String nameEn,
        String nameAr,
        String specialty,
        Integer yearsOfExperience,
        Integer consultationDurationMinutes
) {
}

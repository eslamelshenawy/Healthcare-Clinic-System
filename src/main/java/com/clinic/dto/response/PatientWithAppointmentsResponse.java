package com.clinic.dto.response;

import java.time.LocalDate;
import java.util.List;

public record PatientWithAppointmentsResponse(
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
        List<AppointmentResponse> appointments
) {
}

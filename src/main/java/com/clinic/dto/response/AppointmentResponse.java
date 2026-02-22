package com.clinic.dto.response;

import java.time.LocalDateTime;

public record AppointmentResponse(
        Long id,
        Long patientId,
        String patientName,
        Long doctorId,
        String doctorName,
        LocalDateTime appointmentDateTime,
        String status,
        String reason,
        LocalDateTime createdAt
) {
}

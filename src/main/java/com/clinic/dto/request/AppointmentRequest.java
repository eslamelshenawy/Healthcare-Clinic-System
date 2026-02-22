package com.clinic.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentRequest(

        @NotNull(message = "Patient ID is required")
        Long patientId,

        @NotNull(message = "Doctor ID is required")
        Long doctorId,

        @NotNull(message = "Appointment date and time is required")
        @Future(message = "Appointment must be in the future")
        LocalDateTime appointmentDateTime,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}

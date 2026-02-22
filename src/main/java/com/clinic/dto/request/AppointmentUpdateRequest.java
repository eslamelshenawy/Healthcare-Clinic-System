package com.clinic.dto.request;

import com.clinic.entity.enums.AppointmentStatus;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AppointmentUpdateRequest(

        @Future(message = "Appointment must be in the future")
        LocalDateTime appointmentDateTime,

        AppointmentStatus status,

        @Size(max = 500, message = "Reason must not exceed 500 characters")
        String reason
) {
}

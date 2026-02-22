package com.clinic.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRegistrationRequest(

        @NotBlank(message = "Full name (English) is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String fullNameEn,

        @NotBlank(message = "Full name (Arabic) is required")
        @Size(max = 150, message = "Name must not exceed 150 characters")
        String fullNameAr,

        @NotBlank(message = "Email is required")
        @Email(message = "Email format is invalid")
        String email,

        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^\\+?[0-9]{8,15}$", message = "Invalid mobile number format")
        String mobileNumber,

        @NotNull(message = "Date of birth is required")
        @Past(message = "Date of birth must be in the past")
        LocalDate dateOfBirth,

        @NotBlank(message = "National ID is required")
        @Size(max = 30, message = "National ID must not exceed 30 characters")
        String nationalId,

        @NotBlank(message = "Street is required")
        String street,

        @NotBlank(message = "City is required")
        String city,

        @NotBlank(message = "Region is required")
        String region
) {
}

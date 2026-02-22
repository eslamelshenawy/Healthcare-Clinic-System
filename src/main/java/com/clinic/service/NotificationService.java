package com.clinic.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);

    @Async
    public void sendAppointmentConfirmation(String patientEmail, String patientName,
                                            String doctorName, LocalDateTime dateTime) {
        log.info("Sending appointment confirmation to {} for appointment with Dr. {} at {}",
                patientEmail, doctorName, dateTime);

        // In production, integrate with email service (SendGrid, SES, etc.)
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        log.info("Appointment confirmation sent to {} successfully", patientEmail);
    }
}

package com.example.e_ticket_booking_system.dto.request;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEventScheduleRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;
}

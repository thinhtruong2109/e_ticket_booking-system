package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CheckInRequest {
    @NotBlank(message = "Ticket code is required")
    private String ticketCode;

    private Long scheduleId;
}

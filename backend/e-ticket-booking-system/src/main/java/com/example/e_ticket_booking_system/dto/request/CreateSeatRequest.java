package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSeatRequest {
    @NotNull(message = "Venue ID is required")
    private Long venueId;

    private Long sectionId;

    private String rowNumber;

    @NotBlank(message = "Seat number is required")
    private String seatNumber;

    private String seatType; // VIP, REGULAR, WHEELCHAIR
}

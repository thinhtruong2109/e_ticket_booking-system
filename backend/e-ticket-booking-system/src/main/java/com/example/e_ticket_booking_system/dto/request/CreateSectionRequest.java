package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateSectionRequest {
    @NotNull(message = "Venue ID is required")
    private Long venueId;

    @NotBlank(message = "Section name is required")
    private String name;

    private String description;
    private Integer capacity;
    private Boolean hasNumberedSeats;
}

package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateEventRequest {
    @NotBlank(message = "Event name is required")
    private String name;

    private String description;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    @NotNull(message = "Venue ID is required")
    private Long venueId;

    private String bannerImageUrl;
    private String thumbnailImageUrl;
    private Integer totalTickets;
    private Boolean allowTicketExchange;
}

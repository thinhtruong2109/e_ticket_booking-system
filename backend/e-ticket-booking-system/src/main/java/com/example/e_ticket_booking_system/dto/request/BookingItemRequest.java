package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class BookingItemRequest {
    @NotNull(message = "Ticket type ID is required")
    private Long ticketTypeId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;
}

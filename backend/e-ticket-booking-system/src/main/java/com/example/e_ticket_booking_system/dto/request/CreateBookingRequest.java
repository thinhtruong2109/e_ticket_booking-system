package com.example.e_ticket_booking_system.dto.request;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;

    private Long scheduleId;

    @NotEmpty(message = "At least one booking item is required")
    @Valid
    private List<BookingItemRequest> items;

    private List<Long> seatIds; // Optional, for seat map events
}

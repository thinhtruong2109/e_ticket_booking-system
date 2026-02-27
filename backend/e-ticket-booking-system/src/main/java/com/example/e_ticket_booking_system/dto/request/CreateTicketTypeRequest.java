package com.example.e_ticket_booking_system.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreateTicketTypeRequest {
    @NotNull(message = "Event ID is required")
    private Long eventId;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @NotNull(message = "Total quantity is required")
    @Positive(message = "Total quantity must be positive")
    private Integer totalQuantity;

    private Integer maxPerBooking;
}

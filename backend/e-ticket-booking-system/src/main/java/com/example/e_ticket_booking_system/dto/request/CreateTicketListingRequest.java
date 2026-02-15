package com.example.e_ticket_booking_system.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketListingRequest {
    @NotNull(message = "Ticket ID is required")
    private Long ticketId;

    @NotNull(message = "Listing price is required")
    private BigDecimal listingPrice;

    private String exchangeType; // SELL, TRADE, BOTH (default: SELL)
    private String description;
    private LocalDateTime expiresAt;
}

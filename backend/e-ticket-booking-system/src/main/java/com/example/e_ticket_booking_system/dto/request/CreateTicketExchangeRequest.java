package com.example.e_ticket_booking_system.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateTicketExchangeRequest {
    @NotNull(message = "Ticket listing ID is required")
    private Long ticketListingId;

    private String transactionType; // PURCHASE, TRADE (default: PURCHASE)
    private Long tradeTicketId; // Required if TRADE
    private String paymentMethod; // VNPAY, MOMO, STRIPE (for PURCHASE)
}

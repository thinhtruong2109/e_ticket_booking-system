package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketExchangeResponse {
    private Long id;
    private Long ticketListingId;
    private Long sellerId;
    private String sellerName;
    private Long buyerId;
    private String buyerName;
    private String transactionType;
    private BigDecimal price;
    private Long tradeTicketId;
    private String status;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
}

package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketListingResponse {
    private Long id;
    private Long ticketId;
    private String ticketCode;
    private Long eventId;
    private String eventName;
    private String ticketTypeName;
    private Long sellerId;
    private String sellerName;
    private BigDecimal listingPrice;
    private String exchangeType;
    private String description;
    private String status;
    private LocalDateTime listedAt;
    private LocalDateTime expiresAt;
}

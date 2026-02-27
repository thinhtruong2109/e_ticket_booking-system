package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketTypeResponse {
    private Long id;
    private Long eventId;
    private String name;
    private String description;
    private BigDecimal price;
    private Integer totalQuantity;
    private Integer availableQuantity;
    private Integer maxPerBooking;
}

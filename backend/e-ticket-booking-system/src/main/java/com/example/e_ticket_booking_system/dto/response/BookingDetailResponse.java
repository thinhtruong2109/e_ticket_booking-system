package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailResponse {
    private Long id;
    private Long ticketTypeId;
    private String ticketTypeName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}

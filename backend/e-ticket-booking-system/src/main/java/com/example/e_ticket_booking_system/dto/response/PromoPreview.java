package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoPreview {
    private Long id;
    private String code;
    private String description;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
}

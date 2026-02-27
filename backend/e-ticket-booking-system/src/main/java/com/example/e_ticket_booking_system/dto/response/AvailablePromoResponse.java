package com.example.e_ticket_booking_system.dto.response;

import java.math.BigDecimal;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailablePromoResponse {
    private BigDecimal totalAmount;
    private List<PromoPreview> availablePromoCodes;
}

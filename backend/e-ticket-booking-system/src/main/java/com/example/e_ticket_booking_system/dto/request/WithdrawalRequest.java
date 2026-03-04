package com.example.e_ticket_booking_system.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class WithdrawalRequest {
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "10000", message = "Minimum withdrawal amount is 10,000 VND")
    private BigDecimal amount;
}

package com.example.e_ticket_booking_system.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CreatePromoCodeRequest {
    @NotBlank(message = "Code is required")
    private String code;

    private String description;

    @NotBlank(message = "Discount type is required")
    private String discountType; // PERCENTAGE, FIXED_AMOUNT

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be positive")
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimit;

    @NotNull(message = "Valid from is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid to is required")
    private LocalDateTime validTo;
}

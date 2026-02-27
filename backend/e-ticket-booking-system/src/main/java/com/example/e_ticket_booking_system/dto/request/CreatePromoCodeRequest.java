package com.example.e_ticket_booking_system.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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

    /**
     * Loại áp dụng:
     * - GLOBAL: Áp dụng cho tất cả event (chỉ ADMIN được tạo)
     * - ORGANIZER_ALL: Áp dụng cho tất cả event của organizer hiện tại
     * - SPECIFIC_EVENTS: Chỉ áp dụng cho các event cụ thể (phải truyền eventIds)
     */
    @NotBlank(message = "Application type is required")
    private String applicationType; // GLOBAL, ORGANIZER_ALL, SPECIFIC_EVENTS

    /**
     * Danh sách event IDs mà promo code áp dụng.
     * Chỉ bắt buộc khi applicationType = SPECIFIC_EVENTS.
     * Organizer chỉ được chọn event do chính họ tổ chức.
     */
    private List<Long> eventIds;
}

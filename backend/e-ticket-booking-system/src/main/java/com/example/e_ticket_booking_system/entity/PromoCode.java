package com.example.e_ticket_booking_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "promo_codes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCode {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "discount_type", nullable = false, length = 20)
    private String discountType; // PERCENTAGE, FIXED_AMOUNT
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "min_order_amount", precision = 10, scale = 2)
    private BigDecimal minOrderAmount;
    
    @Column(name = "max_discount_amount", precision = 10, scale = 2)
    private BigDecimal maxDiscountAmount;
    
    @Column(name = "usage_limit")
    private Integer usageLimit;
    
    @Column(name = "used_count")
    private Integer usedCount;
    
    @Column(name = "valid_from", nullable = false)
    private LocalDateTime validFrom;
    
    @Column(name = "valid_to", nullable = false)
    private LocalDateTime validTo;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // ACTIVE, EXPIRED, DISABLED

    /**
     * Loại áp dụng của promo code:
     * - GLOBAL: Admin tạo, áp dụng cho tất cả event
     * - ORGANIZER_ALL: Organizer tạo, áp dụng cho tất cả event của organizer đó
     * - SPECIFIC_EVENTS: Organizer tạo, chỉ áp dụng cho các event cụ thể (qua bảng promo_code_events)
     */
    @Column(name = "application_type", nullable = false, length = 30)
    private String applicationType; // GLOBAL, ORGANIZER_ALL, SPECIFIC_EVENTS

    /**
     * Người tạo promo code.
     * - ADMIN tạo GLOBAL promo codes
     * - ORGANIZER tạo ORGANIZER_ALL hoặc SPECIFIC_EVENTS promo codes
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "created_by", nullable = false,
                foreignKey = @ForeignKey(name = "fk_promo_codes_created_by"))
    private User createdBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (usedCount == null) {
            usedCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

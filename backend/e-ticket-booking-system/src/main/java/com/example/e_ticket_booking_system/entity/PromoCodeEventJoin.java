package com.example.e_ticket_booking_system.entity;

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
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Bảng many-to-many giữa PromoCode và Event.
 * 
 * CHỈ DÙNG KHI PromoCode.applicationType = "SPECIFIC_EVENTS".
 * 
 * Mục đích:
 * - Khi Organizer muốn tạo promo code chỉ áp dụng cho MỘT SỐ event cụ thể
 *   (không phải tất cả event của họ), cần bảng này để lưu danh sách event nào được áp dụng.
 * 
 * Ví dụ:
 * - Organizer A có 5 events (ID: 1, 2, 3, 4, 5)
 * - Tạo promo "SALE20" với applicationType = SPECIFIC_EVENTS
 * - Chỉ muốn áp dụng cho event 2 và 4
 * - → Lưu 2 records: (promo_code_id=SALE20, event_id=2) và (promo_code_id=SALE20, event_id=4)
 * 
 * KHÔNG CẦN bảng này khi:
 * - applicationType = GLOBAL       → áp dụng tất cả event, không cần lưu gì thêm
 * - applicationType = ORGANIZER_ALL → lọc theo createdBy.id == event.organizer.id là đủ
 */
@Entity
@Table(name = "promo_code_events",
       uniqueConstraints = @UniqueConstraint(
           name = "uk_promo_code_event",
           columnNames = {"promo_code_id", "event_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PromoCodeEventJoin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "promo_code_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_promo_code_events_promo_code_id"))
    private PromoCode promoCode;

    @ManyToOne(optional = false)
    @JoinColumn(name = "event_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_promo_code_events_event_id"))
    private Event event;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

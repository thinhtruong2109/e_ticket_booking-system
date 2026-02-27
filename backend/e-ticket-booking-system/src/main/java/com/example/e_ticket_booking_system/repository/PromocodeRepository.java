package com.example.e_ticket_booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.PromoCode;

@Repository
public interface PromocodeRepository extends JpaRepository<PromoCode, Long> {
    PromoCode findByCode(String code);
    List<PromoCode> findByStatus(String status);
    List<PromoCode> findByValidFromLessThanAndValidToGreaterThan(LocalDateTime from, LocalDateTime to);

    /** Tìm tất cả promo code do một user tạo */
    List<PromoCode> findByCreatedById(Long userId);

    /** Tìm promo code do một user tạo với status cụ thể */
    List<PromoCode> findByCreatedByIdAndStatus(Long userId, String status);

    /** Tìm promo code theo applicationType và status */
    List<PromoCode> findByApplicationTypeAndStatus(String applicationType, String status);

    /** Tìm promo code ACTIVE theo applicationType và createdBy */
    List<PromoCode> findByApplicationTypeAndCreatedByIdAndStatus(String applicationType, Long createdById, String status);

    /**
     * Tìm ACTIVE promo codes theo applicationType, status, và thời hạn còn hiệu lực.
     * Dùng cho: GLOBAL (tất cả events) và ORGANIZER_ALL (tất cả events của organizer).
     */
    List<PromoCode> findByApplicationTypeAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            String applicationType, String status, LocalDateTime now1, LocalDateTime now2);

    /**
     * Tìm ACTIVE promo codes theo applicationType, createdBy, status, và thời hạn.
     * Dùng cho: ORGANIZER_ALL — lọc theo organizer cụ thể.
     */
    List<PromoCode> findByApplicationTypeAndCreatedByIdAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            String applicationType, Long createdById, String status, LocalDateTime now1, LocalDateTime now2);

    /**
     * Tìm ACTIVE promo codes theo danh sách IDs, status, và thời hạn.
     * Dùng cho: SPECIFIC_EVENTS — sau khi đã lấy promoCodeIds từ PromoCodeEventJoinRepository.
     */
    List<PromoCode> findByIdInAndStatusAndValidFromLessThanEqualAndValidToGreaterThanEqual(
            List<Long> ids, String status, LocalDateTime now1, LocalDateTime now2);
}

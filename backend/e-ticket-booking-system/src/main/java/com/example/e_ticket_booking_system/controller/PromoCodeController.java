package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.dto.request.AvailablePromoRequest;
import com.example.e_ticket_booking_system.dto.request.CreatePromoCodeRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.AvailablePromoResponse;
import com.example.e_ticket_booking_system.dto.response.PromoCodeResponse;
import com.example.e_ticket_booking_system.service.PromoCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/promo-codes")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    // ======================== ADMIN endpoints ========================

    /** Admin tạo promo code (chỉ GLOBAL) */
    @PostMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromoCodeByAdmin(
            @Valid @RequestBody CreatePromoCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promo code created",
                promoCodeService.createPromoCodeByAdmin(request)));
    }

    /** Admin xem tất cả promo codes */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getAllPromoCodes() {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getAllPromoCodes()));
    }

    /** Admin xem promo codes đang active */
    @GetMapping("/admin/active")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getActivePromoCodes() {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getActivePromoCodes()));
    }

    /** Admin xem promo code theo ID */
    @GetMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getPromoCodeById(id)));
    }

    /** Admin cập nhật promo code */
    @PutMapping("/admin/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> updatePromoCodeByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody CreatePromoCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promo code updated",
                promoCodeService.updatePromoCodeByAdmin(id, request)));
    }

    /** Admin deactivate promo code bất kỳ */
    @PutMapping("/admin/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> deactivatePromoCode(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Promo code deactivated",
                promoCodeService.deactivatePromoCode(id)));
    }

    // ======================== ORGANIZER endpoints ========================

    /** Organizer tạo promo code (ORGANIZER_ALL hoặc SPECIFIC_EVENTS) */
    @PostMapping("/organizer")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromoCodeByOrganizer(
            @Valid @RequestBody CreatePromoCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promo code created",
                promoCodeService.createPromoCodeByOrganizer(request)));
    }

    /** Organizer xem tất cả promo codes của mình */
    @GetMapping("/organizer")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getPromoCodesByOrganizer() {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getPromoCodesByOrganizer()));
    }

    /** Organizer xem promo code theo ID (chỉ của mình) */
    @GetMapping("/organizer/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCodeByIdForOrganizer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getPromoCodeByIdForOrganizer(id)));
    }

    /** Organizer cập nhật promo code của mình */
    @PutMapping("/organizer/{id}")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> updatePromoCodeByOrganizer(
            @PathVariable Long id,
            @Valid @RequestBody CreatePromoCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promo code updated",
                promoCodeService.updatePromoCodeByOrganizer(id, request)));
    }

    /** Organizer deactivate promo code của mình */
    @PutMapping("/organizer/{id}/deactivate")
    @PreAuthorize("hasRole('ORGANIZER')")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> deactivatePromoCodeByOrganizer(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Promo code deactivated",
                promoCodeService.deactivatePromoCodeByOrganizer(id)));
    }

    // ======================== PUBLIC/CUSTOMER endpoints ========================

    /**
     * Lấy danh sách promo codes khả dụng cho một order cụ thể.
     * Bao gồm: GLOBAL codes + ORGANIZER_ALL codes (của organizer sự kiện) + SPECIFIC_EVENTS codes (gắn event).
     * Authenticated users (bao gồm CUSTOMER) đều có thể gọi.
     */
    @PostMapping("/available")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<AvailablePromoResponse>> getAvailablePromoCodes(
            @Valid @RequestBody AvailablePromoRequest request) {
        return ResponseEntity.ok(ApiResponse.success(
                promoCodeService.getAvailablePromoCodes(request)));
    }
}

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

import com.example.e_ticket_booking_system.dto.request.CreatePromoCodeRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.PromoCodeResponse;
import com.example.e_ticket_booking_system.service.PromoCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/promo-codes")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class PromoCodeController {

    private final PromoCodeService promoCodeService;

    @PostMapping
    public ResponseEntity<ApiResponse<PromoCodeResponse>> createPromoCode(
            @Valid @RequestBody CreatePromoCodeRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Promo code created",
                promoCodeService.createPromoCode(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getAllPromoCodes() {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getAllPromoCodes()));
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<PromoCodeResponse>>> getActivePromoCodes() {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getActivePromoCodes()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> getPromoCodeById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(promoCodeService.getPromoCodeById(id)));
    }

    @PutMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<PromoCodeResponse>> deactivatePromoCode(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("Promo code deactivated",
                promoCodeService.deactivatePromoCode(id)));
    }
}

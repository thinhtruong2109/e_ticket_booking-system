package com.example.e_ticket_booking_system.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.CreatePaymentRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.PaymentResponse;
import com.example.e_ticket_booking_system.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final SecurityUtils securityUtils;

    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Payment created",
                paymentService.createPayment(userId, request)));
    }

    @PostMapping("/callback")
    public ResponseEntity<ApiResponse<PaymentResponse>> paymentCallback(
            @RequestParam String transactionId,
            @RequestParam boolean success) {
        return ResponseEntity.ok(ApiResponse.success(
                paymentService.processPaymentCallback(transactionId, success)));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByBooking(
            @RequestParam Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(paymentService.getPaymentByBookingId(bookingId)));
    }
}

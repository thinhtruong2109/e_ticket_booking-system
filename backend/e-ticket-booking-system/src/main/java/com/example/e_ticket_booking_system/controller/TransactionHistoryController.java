package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.TransactionHistoryResponse;
import com.example.e_ticket_booking_system.service.TransactionHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/transaction-histories")
@RequiredArgsConstructor
public class TransactionHistoryController {

    private final TransactionHistoryService transactionHistoryService;
    private final SecurityUtils securityUtils;

    /**
     * Lấy lịch sử giao dịch của user đang đăng nhập
     */
    @GetMapping("/my-transactions")
    public ResponseEntity<ApiResponse<List<TransactionHistoryResponse>>> getMyTransactions(
            @RequestParam(required = false) String type) {
        Long userId = securityUtils.getCurrentUserId();
        List<TransactionHistoryResponse> transactions;
        if (type != null && !type.isBlank()) {
            transactions = transactionHistoryService.getMyTransactionsByType(userId, type.toUpperCase());
        } else {
            transactions = transactionHistoryService.getMyTransactions(userId);
        }
        return ResponseEntity.ok(ApiResponse.success(transactions));
    }

    /**
     * Lấy lịch sử giao dịch theo booking ID (user chỉ xem booking của mình)
     */
    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<TransactionHistoryResponse>>> getTransactionsByBooking(
            @PathVariable Long bookingId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionHistoryService.getTransactionsByBookingId(bookingId)));
    }

    /**
     * Lấy lịch sử giao dịch theo payment ID
     */
    @GetMapping("/payment/{paymentId}")
    public ResponseEntity<ApiResponse<List<TransactionHistoryResponse>>> getTransactionsByPayment(
            @PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionHistoryService.getTransactionsByPaymentId(paymentId)));
    }

    /**
     * [ADMIN] Lấy tất cả lịch sử giao dịch
     */
    @GetMapping("/admin")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<TransactionHistoryResponse>>> getAllTransactions() {
        return ResponseEntity.ok(ApiResponse.success(
                transactionHistoryService.getAllTransactions()));
    }

    /**
     * [ADMIN] Lấy lịch sử giao dịch của user bất kỳ
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<List<TransactionHistoryResponse>>> getTransactionsByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                transactionHistoryService.getTransactionsByUserId(userId)));
    }
}

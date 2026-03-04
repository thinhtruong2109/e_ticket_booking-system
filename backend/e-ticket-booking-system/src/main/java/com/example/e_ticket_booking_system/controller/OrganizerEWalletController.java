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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.UpdateBankInfoRequest;
import com.example.e_ticket_booking_system.dto.request.WithdrawalRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.OrganizerEWalletResponse;
import com.example.e_ticket_booking_system.dto.response.WalletTransactionResponse;
import com.example.e_ticket_booking_system.service.OrganizerEWalletService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/organizer/wallet")
@RequiredArgsConstructor
public class OrganizerEWalletController {

    private final OrganizerEWalletService walletService;
    private final SecurityUtils securityUtils;

    // ==================== ORGANIZER ENDPOINTS ====================

    /**
     * Lấy thông tin ví của organizer đang đăng nhập.
     * Nếu chưa có ví → tự động tạo mới.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrganizerEWalletResponse>> getMyWallet() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(walletService.getMyWallet(userId)));
    }

    /**
     * Cập nhật thông tin ngân hàng cho ví organizer.
     */
    @PutMapping("/bank-info")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<OrganizerEWalletResponse>> updateBankInfo(
            @Valid @RequestBody UpdateBankInfoRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Bank info updated",
                walletService.updateBankInfo(userId, request)));
    }

    /**
     * Yêu cầu rút tiền từ ví organizer.
     */
    @PostMapping("/withdraw")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<WalletTransactionResponse>> requestWithdrawal(
            @Valid @RequestBody WithdrawalRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful",
                walletService.requestWithdrawal(userId, request)));
    }

    /**
     * Lấy lịch sử giao dịch ví của organizer đang đăng nhập.
     */
    @GetMapping("/transactions")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getMyWalletTransactions(
            @RequestParam(required = false) String type) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(
                walletService.getMyWalletTransactions(userId, type)));
    }

    // ==================== ADMIN ENDPOINTS ====================

    /**
     * [ADMIN] Lấy danh sách tất cả ví organizer.
     */
    @GetMapping("/admin/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<OrganizerEWalletResponse>>> getAllWallets() {
        return ResponseEntity.ok(ApiResponse.success(walletService.getAllWallets()));
    }

    /**
     * [ADMIN] Lấy thông tin ví của organizer bất kỳ.
     */
    @GetMapping("/admin/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<OrganizerEWalletResponse>> getWalletByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(walletService.getWalletByUserId(userId)));
    }

    /**
     * [ADMIN] Lấy lịch sử giao dịch ví của organizer bất kỳ.
     */
    @GetMapping("/admin/user/{userId}/transactions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<WalletTransactionResponse>>> getWalletTransactionsByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(
                walletService.getWalletTransactionsByUserId(userId)));
    }
}

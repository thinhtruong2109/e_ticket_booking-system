package com.example.e_ticket_booking_system.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.UpdateBankInfoRequest;
import com.example.e_ticket_booking_system.dto.request.WithdrawalRequest;
import com.example.e_ticket_booking_system.dto.response.OrganizerEWalletResponse;
import com.example.e_ticket_booking_system.dto.response.WalletTransactionResponse;
import com.example.e_ticket_booking_system.entity.OrganizerEWallet;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.entity.WalletTransaction;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.OrganizerEWalletRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;
import com.example.e_ticket_booking_system.repository.WalletTransactionRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OrganizerEWalletService {

    private static final Logger log = LoggerFactory.getLogger(OrganizerEWalletService.class);

    private final OrganizerEWalletRepository walletRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    // ==================== QUERY OPERATIONS ====================

    /**
     * Lấy thông tin ví của organizer hiện tại.
     * Nếu chưa có ví → tự động tạo mới.
     */
    public OrganizerEWalletResponse getMyWallet(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateOrganizer(user);

        OrganizerEWallet wallet = getOrCreateWallet(user);
        return toResponse(wallet);
    }

    /**
     * Lấy lịch sử giao dịch ví của organizer hiện tại.
     */
    public List<WalletTransactionResponse> getMyWalletTransactions(Long userId, String type) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateOrganizer(user);

        OrganizerEWallet wallet = getOrCreateWallet(user);

        List<WalletTransaction> transactions;
        if (type != null && !type.isBlank()) {
            transactions = walletTransactionRepository
                    .findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(wallet.getId(), type.toUpperCase());
        } else {
            transactions = walletTransactionRepository
                    .findByWalletIdOrderByCreatedAtDesc(wallet.getId());
        }

        return transactions.stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }

    // ==================== BANK INFO ====================

    /**
     * Cập nhật thông tin ngân hàng cho ví organizer.
     */
    @Transactional
    public OrganizerEWalletResponse updateBankInfo(Long userId, UpdateBankInfoRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateOrganizer(user);

        OrganizerEWallet wallet = getOrCreateWallet(user);
        wallet.setBankName(request.getBankName());
        wallet.setBankAccountNumber(request.getBankAccountNumber());
        wallet.setBankAccountHolder(request.getBankAccountHolder());
        walletRepository.save(wallet);

        log.info("Bank info updated for organizer userId: {}", userId);
        return toResponse(wallet);
    }

    // ==================== WITHDRAWAL ====================

    /**
     * Yêu cầu rút tiền từ ví organizer.
     * - Validate đủ số dư
     * - Validate đã cập nhật thông tin ngân hàng
     * - Trừ balance, cộng totalWithdrawn
     * - Ghi log WalletTransaction
     *
     * Lưu ý: Trong thực tế, rút tiền có thể cần trạng thái PENDING → chờ admin duyệt.
     * Ở version này, rút tiền xử lý ngay (SUCCESS) để đơn giản hóa flow.
     */
    @Transactional
    public WalletTransactionResponse requestWithdrawal(Long userId, WithdrawalRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        validateOrganizer(user);

        OrganizerEWallet wallet = getOrCreateWallet(user);

        // Validate bank info
        if (wallet.getBankName() == null || wallet.getBankAccountNumber() == null) {
            throw new BadRequestException("Please update your bank information before withdrawing");
        }

        BigDecimal amount = request.getAmount();

        // Validate balance
        if (wallet.getBalance().compareTo(amount) < 0) {
            throw new BadRequestException("Insufficient balance. Current balance: " + wallet.getBalance());
        }

        // Trừ balance, cộng totalWithdrawn
        wallet.setBalance(wallet.getBalance().subtract(amount));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(amount));
        walletRepository.save(wallet);

        // Ghi log giao dịch rút tiền
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType("WITHDRAWAL");
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getBalance());
        tx.setDescription("Rút tiền về " + wallet.getBankName() + " - " + wallet.getBankAccountNumber());
        tx.setStatus("SUCCESS");
        walletTransactionRepository.save(tx);

        log.info("Withdrawal {} VND from wallet of organizer userId: {}, new balance: {}",
                amount, userId, wallet.getBalance());

        return toTransactionResponse(tx);
    }

    // ==================== INTERNAL: Credit revenue ====================

    /**
     * Cộng doanh thu vào ví organizer khi booking payment thành công.
     * Được gọi từ PaymentService sau khi payment SUCCESS.
     *
     * @param organizerId ID của user organizer (event owner)
     * @param amount      Số tiền cộng vào (finalAmount của booking)
     * @param bookingCode Mã booking tham chiếu
     */
    @Transactional
    public void creditRevenue(Long organizerId, BigDecimal amount, String bookingCode) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        OrganizerEWallet wallet = getOrCreateWallet(organizer);

        // Cộng balance
        wallet.setBalance(wallet.getBalance().add(amount));
        walletRepository.save(wallet);

        // Ghi log giao dịch revenue
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType("REVENUE");
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getBalance());
        tx.setDescription("Doanh thu từ booking " + bookingCode);
        tx.setReferenceCode(bookingCode);
        tx.setStatus("SUCCESS");
        walletTransactionRepository.save(tx);

        log.info("Credited {} VND to wallet of organizer userId: {} for booking {}, new balance: {}",
                amount, organizerId, bookingCode, wallet.getBalance());
    }

    /**
     * Trừ tiền khỏi ví organizer khi hoàn vé cho khách (refund).
     * Được gọi từ PaymentService khi refund payment.
     *
     * @param organizerId ID của user organizer (event owner)
     * @param amount      Số tiền trừ
     * @param bookingCode Mã booking tham chiếu
     */
    @Transactional
    public void deductRefund(Long organizerId, BigDecimal amount, String bookingCode) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found"));

        OrganizerEWallet wallet = getOrCreateWallet(organizer);

        // Trừ balance (có thể âm nếu organizer đã rút tiền)
        wallet.setBalance(wallet.getBalance().subtract(amount));
        walletRepository.save(wallet);

        // Ghi log giao dịch refund deduction
        WalletTransaction tx = new WalletTransaction();
        tx.setWallet(wallet);
        tx.setTransactionType("REFUND_DEDUCTION");
        tx.setAmount(amount);
        tx.setBalanceAfter(wallet.getBalance());
        tx.setDescription("Trừ tiền hoàn vé booking " + bookingCode);
        tx.setReferenceCode(bookingCode);
        tx.setStatus("SUCCESS");
        walletTransactionRepository.save(tx);

        log.info("Deducted {} VND from wallet of organizer userId: {} for refund booking {}, new balance: {}",
                amount, organizerId, bookingCode, wallet.getBalance());
    }

    // ==================== ADMIN OPERATIONS ====================

    /**
     * [ADMIN] Lấy thông tin ví của bất kỳ organizer nào.
     */
    public OrganizerEWalletResponse getWalletByUserId(Long userId) {
        OrganizerEWallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for userId: " + userId));
        return toResponse(wallet);
    }

    /**
     * [ADMIN] Lấy danh sách tất cả ví organizer.
     */
    public List<OrganizerEWalletResponse> getAllWallets() {
        return walletRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * [ADMIN] Lấy lịch sử giao dịch ví của bất kỳ organizer nào.
     */
    public List<WalletTransactionResponse> getWalletTransactionsByUserId(Long userId) {
        return walletTransactionRepository.findByWalletUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toTransactionResponse).collect(Collectors.toList());
    }

    // ==================== HELPERS ====================

    private OrganizerEWallet getOrCreateWallet(User user) {
        return walletRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    OrganizerEWallet newWallet = new OrganizerEWallet();
                    newWallet.setUser(user);
                    newWallet.setBalance(BigDecimal.ZERO);
                    newWallet.setTotalWithdrawn(BigDecimal.ZERO);
                    walletRepository.save(newWallet);
                    log.info("Created new eWallet for organizer userId: {}", user.getId());
                    return newWallet;
                });
    }

    private void validateOrganizer(User user) {
        if (!"ORGANIZER".equals(user.getRole()) && !"ADMIN".equals(user.getRole())) {
            throw new ForbiddenException("Only organizers can access eWallet features");
        }
    }

    private OrganizerEWalletResponse toResponse(OrganizerEWallet wallet) {
        OrganizerEWalletResponse r = new OrganizerEWalletResponse();
        r.setId(wallet.getId());
        r.setUserId(wallet.getUser().getId());
        r.setUserFullName(wallet.getUser().getFullName());
        r.setBalance(wallet.getBalance());
        r.setTotalWithdrawn(wallet.getTotalWithdrawn());
        r.setBankName(wallet.getBankName());
        r.setBankAccountNumber(wallet.getBankAccountNumber());
        r.setBankAccountHolder(wallet.getBankAccountHolder());
        r.setCreatedAt(wallet.getCreatedAt());
        r.setUpdatedAt(wallet.getUpdatedAt());
        return r;
    }

    private WalletTransactionResponse toTransactionResponse(WalletTransaction tx) {
        WalletTransactionResponse r = new WalletTransactionResponse();
        r.setId(tx.getId());
        r.setWalletId(tx.getWallet().getId());
        r.setTransactionType(tx.getTransactionType());
        r.setAmount(tx.getAmount());
        r.setBalanceAfter(tx.getBalanceAfter());
        r.setDescription(tx.getDescription());
        r.setReferenceCode(tx.getReferenceCode());
        r.setStatus(tx.getStatus());
        r.setCreatedAt(tx.getCreatedAt());
        return r;
    }
}

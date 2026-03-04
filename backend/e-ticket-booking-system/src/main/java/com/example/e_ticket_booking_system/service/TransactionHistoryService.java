package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.response.TransactionHistoryResponse;
import com.example.e_ticket_booking_system.entity.Payment;
import com.example.e_ticket_booking_system.entity.TransactionHistory;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.TransactionHistoryRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TransactionHistoryService {

    private static final Logger log = LoggerFactory.getLogger(TransactionHistoryService.class);

    private final TransactionHistoryRepository transactionHistoryRepository;
    private final UserRepository userRepository;

    // ============================
    // Ghi log giao dịch (internal)
    // ============================

    /**
     * Ghi log khi tạo payment mới (PENDING)
     */
    @Transactional
    public void logPaymentCreated(Payment payment) {
        TransactionHistory history = new TransactionHistory();
        history.setPayment(payment);
        history.setUser(payment.getBooking().getCustomer());
        history.setTransactionType("PAYMENT");
        history.setStatus("PENDING");
        history.setAmount(payment.getAmount());
        history.setDescription("Tạo thanh toán cho booking " + payment.getBooking().getBookingCode());
        history.setPaymentMethod(payment.getPaymentMethod());
        history.setBooking(payment.getBooking());

        transactionHistoryRepository.save(history);
        log.info("Transaction log: PAYMENT PENDING for booking {}", payment.getBooking().getBookingCode());
    }

    /**
     * Ghi log khi thanh toán thành công
     */
    @Transactional
    public void logPaymentSuccess(Payment payment) {
        TransactionHistory history = new TransactionHistory();
        history.setPayment(payment);
        history.setUser(payment.getBooking().getCustomer());
        history.setTransactionType("PAYMENT");
        history.setStatus("SUCCESS");
        history.setAmount(payment.getAmount());
        history.setDescription("Thanh toán thành công booking " + payment.getBooking().getBookingCode());
        history.setPaymentMethod(payment.getPaymentMethod());
        history.setBooking(payment.getBooking());

        transactionHistoryRepository.save(history);
        log.info("Transaction log: PAYMENT SUCCESS for booking {}", payment.getBooking().getBookingCode());
    }

    /**
     * Ghi log khi thanh toán thất bại
     */
    @Transactional
    public void logPaymentFailed(Payment payment) {
        TransactionHistory history = new TransactionHistory();
        history.setPayment(payment);
        history.setUser(payment.getBooking().getCustomer());
        history.setTransactionType("PAYMENT");
        history.setStatus("FAILED");
        history.setAmount(payment.getAmount());
        history.setDescription("Thanh toán thất bại cho booking " + payment.getBooking().getBookingCode());
        history.setPaymentMethod(payment.getPaymentMethod());
        history.setBooking(payment.getBooking());

        transactionHistoryRepository.save(history);
        log.info("Transaction log: PAYMENT FAILED for booking {}", payment.getBooking().getBookingCode());
    }

    /**
     * Ghi log khi thanh toán bị hủy
     */
    @Transactional
    public void logPaymentCancelled(Payment payment) {
        TransactionHistory history = new TransactionHistory();
        history.setPayment(payment);
        history.setUser(payment.getBooking().getCustomer());
        history.setTransactionType("PAYMENT");
        history.setStatus("CANCELLED");
        history.setAmount(payment.getAmount());
        history.setDescription("Hủy thanh toán cho booking " + payment.getBooking().getBookingCode());
        history.setPaymentMethod(payment.getPaymentMethod());
        history.setBooking(payment.getBooking());

        transactionHistoryRepository.save(history);
        log.info("Transaction log: PAYMENT CANCELLED for booking {}", payment.getBooking().getBookingCode());
    }

    /**
     * Ghi log khi hoàn tiền
     */
    @Transactional
    public void logRefund(Payment payment) {
        TransactionHistory history = new TransactionHistory();
        history.setPayment(payment);
        history.setUser(payment.getBooking().getCustomer());
        history.setTransactionType("REFUND");
        history.setStatus("SUCCESS");
        history.setAmount(payment.getAmount());
        history.setDescription("Hoàn tiền booking " + payment.getBooking().getBookingCode());
        history.setPaymentMethod(payment.getPaymentMethod());
        history.setBooking(payment.getBooking());

        transactionHistoryRepository.save(history);
        log.info("Transaction log: REFUND SUCCESS for booking {}", payment.getBooking().getBookingCode());
    }

    /**
     * Ghi log cho giao dịch marketplace (mua vé từ listing)
     */
    @Transactional
    public void logExchangePayment(Payment payment, User buyer, String status, String description) {
        TransactionHistory history = new TransactionHistory();
        history.setPayment(payment);
        history.setUser(buyer);
        history.setTransactionType("EXCHANGE_PAYMENT");
        history.setStatus(status);
        history.setAmount(payment.getAmount());
        history.setDescription(description);
        history.setPaymentMethod(payment.getPaymentMethod());
        history.setBooking(payment.getBooking());

        transactionHistoryRepository.save(history);
        log.info("Transaction log: EXCHANGE_PAYMENT {} for orderCode {}", status, payment.getPayosOrderCode());
    }

    // ============================
    // Query API (public)
    // ============================

    /**
     * Lấy lịch sử giao dịch của user đang đăng nhập
     */
    public List<TransactionHistoryResponse> getMyTransactions(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            throw new ResourceNotFoundException("User not found");
        }
        return transactionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử giao dịch của user đang đăng nhập, lọc theo loại
     */
    public List<TransactionHistoryResponse> getMyTransactionsByType(Long userId, String transactionType) {
        return transactionHistoryRepository.findByUserIdAndTransactionTypeOrderByCreatedAtDesc(userId, transactionType)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử giao dịch theo booking ID
     */
    public List<TransactionHistoryResponse> getTransactionsByBookingId(Long bookingId) {
        return transactionHistoryRepository.findByBookingIdOrderByCreatedAtDesc(bookingId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy lịch sử giao dịch theo payment ID
     */
    public List<TransactionHistoryResponse> getTransactionsByPaymentId(Long paymentId) {
        return transactionHistoryRepository.findByPaymentIdOrderByCreatedAtDesc(paymentId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * [ADMIN] Lấy tất cả lịch sử giao dịch
     */
    public List<TransactionHistoryResponse> getAllTransactions() {
        return transactionHistoryRepository.findAll()
                .stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * [ADMIN] Lấy lịch sử giao dịch của user bất kỳ
     */
    public List<TransactionHistoryResponse> getTransactionsByUserId(Long userId) {
        return transactionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ============================
    // Mapper
    // ============================

    private TransactionHistoryResponse toResponse(TransactionHistory history) {
        TransactionHistoryResponse response = new TransactionHistoryResponse();
        response.setId(history.getId());
        response.setPaymentId(history.getPayment().getId());
        response.setUserId(history.getUser().getId());
        response.setUserFullName(history.getUser().getFullName());
        response.setTransactionType(history.getTransactionType());
        response.setStatus(history.getStatus());
        response.setAmount(history.getAmount());
        response.setDescription(history.getDescription());
        response.setPaymentMethod(history.getPaymentMethod());
        if (history.getBooking() != null) {
            response.setBookingId(history.getBooking().getId());
            response.setBookingCode(history.getBooking().getBookingCode());
        }
        response.setCreatedAt(history.getCreatedAt());
        return response;
    }
}

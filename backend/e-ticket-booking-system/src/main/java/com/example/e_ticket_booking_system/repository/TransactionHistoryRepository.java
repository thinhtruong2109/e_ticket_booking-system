package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.TransactionHistory;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    /**
     * Lấy lịch sử giao dịch theo payment ID, sắp xếp mới nhất trước
     */
    List<TransactionHistory> findByPaymentIdOrderByCreatedAtDesc(Long paymentId);

    /**
     * Lấy lịch sử giao dịch theo user ID, sắp xếp mới nhất trước
     */
    List<TransactionHistory> findByUserIdOrderByCreatedAtDesc(Long userId);

    /**
     * Lấy lịch sử giao dịch theo booking ID, sắp xếp mới nhất trước
     */
    List<TransactionHistory> findByBookingIdOrderByCreatedAtDesc(Long bookingId);

    /**
     * Lấy lịch sử giao dịch theo loại giao dịch
     */
    List<TransactionHistory> findByTransactionTypeOrderByCreatedAtDesc(String transactionType);

    /**
     * Lấy lịch sử giao dịch theo user ID và loại giao dịch
     */
    List<TransactionHistory> findByUserIdAndTransactionTypeOrderByCreatedAtDesc(Long userId, String transactionType);
}

package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.WalletTransaction;

@Repository
public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, Long> {
    List<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId);
    List<WalletTransaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(Long walletId, String transactionType);
    List<WalletTransaction> findByWalletUserIdOrderByCreatedAtDesc(Long userId);
}

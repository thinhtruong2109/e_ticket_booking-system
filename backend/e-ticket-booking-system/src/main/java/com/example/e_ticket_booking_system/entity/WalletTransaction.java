package com.example.e_ticket_booking_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Immutable log ghi nhận mọi biến động số dư ví Organizer.
 * Mỗi lần balance thay đổi (revenue vào, rút tiền, hoàn tiền) → tạo 1 record mới.
 * KHÔNG BAO GIỜ update/delete record đã tạo.
 */
@Entity
@Table(name = "wallet_transactions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "wallet_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_wallet_transactions_wallet_id"))
    private OrganizerEWallet wallet;

    /**
     * Loại giao dịch:
     * - REVENUE: Doanh thu từ bán vé (tiền vào ví)
     * - WITHDRAWAL: Rút tiền về tài khoản ngân hàng (tiền ra ví)
     * - REFUND_DEDUCTION: Trừ tiền khi hoàn vé cho khách (tiền ra ví)
     */
    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    /**
     * Số tiền giao dịch (luôn dương).
     * REVENUE: +amount vào balance
     * WITHDRAWAL / REFUND_DEDUCTION: -amount khỏi balance
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Số dư ví SAU giao dịch (snapshot)
     */
    @Column(name = "balance_after", nullable = false, precision = 15, scale = 2)
    private BigDecimal balanceAfter;

    /**
     * Mô tả giao dịch
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Mã tham chiếu (bookingCode, withdrawalId, ...)
     */
    @Column(name = "reference_code", length = 50)
    private String referenceCode;

    /**
     * Trạng thái: SUCCESS, PENDING, FAILED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * Immutable: chỉ có createdAt
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

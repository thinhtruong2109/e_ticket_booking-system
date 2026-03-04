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
 * Lịch sử giao dịch - Immutable log ghi nhận mọi thay đổi trạng thái thanh toán.
 * Mỗi lần payment chuyển trạng thái (PENDING → SUCCESS, SUCCESS → REFUNDED, ...) sẽ tạo 1 record mới.
 * KHÔNG BAO GIỜ update/delete record đã tạo.
 */
@Entity
@Table(name = "transaction_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Payment liên quan đến giao dịch này
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "payment_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transaction_histories_payment_id"))
    private Payment payment;

    /**
     * User thực hiện hoặc liên quan đến giao dịch (người mua/người được hoàn tiền)
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_transaction_histories_user_id"))
    private User user;

    /**
     * Loại giao dịch:
     * - PAYMENT: Thanh toán booking
     * - REFUND: Hoàn tiền booking
     * - EXCHANGE_PAYMENT: Thanh toán mua vé trên marketplace
     * - EXCHANGE_REFUND: Hoàn tiền giao dịch marketplace
     */
    @Column(name = "transaction_type", nullable = false, length = 30)
    private String transactionType;

    /**
     * Trạng thái giao dịch: PENDING, SUCCESS, FAILED, CANCELLED
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status;

    /**
     * Số tiền giao dịch
     */
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    /**
     * Mô tả giao dịch (VD: "Thanh toán booking BK20260303001", "Hoàn tiền booking BK20260303001")
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * Phương thức thanh toán (PAYOS, VNPAY, MOMO, ...)
     */
    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    /**
     * Booking ID liên quan (để query nhanh mà không cần join qua Payment)
     */
    @ManyToOne
    @JoinColumn(name = "booking_id",
                foreignKey = @ForeignKey(name = "fk_transaction_histories_booking_id"))
    private Booking booking;

    /**
     * Immutable: chỉ có createdAt, KHÔNG có updatedAt
     */
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_exchanges")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketExchange {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * Tham chiếu đến TicketListing - vé được trao đổi
     * BUSINESS RULE: Ticket trong TicketListing không thể:
     * 1. Đã check-in (isCheckedIn = true)
     * 2. Listing status phải = FOR_SALE
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_listing_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ticket_exchanges_ticket_listing_id"))
    private TicketListing ticketListing;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "seller_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ticket_exchanges_seller_id"))
    private User seller;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "buyer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ticket_exchanges_buyer_id"))
    private User buyer;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // PURCHASE, TRADE
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @ManyToOne
    @JoinColumn(name = "trade_ticket_id",
                foreignKey = @ForeignKey(name = "fk_ticket_exchanges_trade_ticket_id"))
    private Ticket tradeTicket;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, PAYMENT_PENDING, COMPLETED, CANCELLED, FAILED
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // VNPAY, MOMO, STRIPE, DIRECT_TRADE
    
    @OneToOne
    @JoinColumn(name = "payment_id",
                foreignKey = @ForeignKey(name = "fk_ticket_exchanges_payment_id"))
    private Payment payment;
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

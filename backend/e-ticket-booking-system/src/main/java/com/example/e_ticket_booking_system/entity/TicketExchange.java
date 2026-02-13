package com.example.e_ticket_booking_system.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    
    @Column(name = "ticket_listing_id", nullable = false)
    private Long ticketListingId;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;
    
    @Column(name = "transaction_type", nullable = false, length = 20)
    private String transactionType; // PURCHASE, TRADE
    
    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "trade_ticket_id")
    private Long tradeTicketId; // Vé được trao đổi nếu là TRADE
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // PENDING, PAYMENT_PENDING, COMPLETED, CANCELLED, FAILED
    
    @Column(name = "payment_method", length = 50)
    private String paymentMethod; // VNPAY, MOMO, STRIPE, DIRECT_TRADE
    
    @Column(name = "payment_id")
    private Long paymentId;
    
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

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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_listings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketListing {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
        /**
     * FOREIGN KEY CONSTRAINT: ticket_id
     * BUSINESS RULE: Ticket không thể listed nếu:
     * 1. Ticket.isCheckedIn = true (vé đã sử dụng)
     * 2. Ticket.isTransferable = false (vé không được phép chuyển nhượng)
     * 
     * Validation: Phải được kiểm tra tại Service Layer trước khi persist
     */    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false, 
                foreignKey = @ForeignKey(name = "fk_ticket_listings_ticket_id"))
    private Ticket ticket;
    
    @Column(name = "seller_id", nullable = false)
    private Long sellerId;
    
    @Column(name = "listing_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal listingPrice;
    
    @Column(name = "exchange_type", nullable = false, length = 20)
    private String exchangeType; // SELL, TRADE, BOTH
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // FOR_SALE, SOLD, CANCELLED, EXPIRED
    
    @Column(name = "listed_at", nullable = false)
    private LocalDateTime listedAt;
    
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (listedAt == null) {
            listedAt = LocalDateTime.now();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

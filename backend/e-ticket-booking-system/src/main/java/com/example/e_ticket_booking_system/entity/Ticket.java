package com.example.e_ticket_booking_system.entity;

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
@Table(name = "tickets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "ticket_code", nullable = false, unique = true, length = 50)
    private String ticketCode;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "booking_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_tickets_booking_id"))
    private Booking booking;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_type_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_tickets_ticket_type_id"))
    private TicketType ticketType;
    
    @Column(name = "qr_code", nullable = false, unique = true, columnDefinition = "TEXT")
    private String qrCode;
    
    /**
     * Track chủ sở hữu hiện tại của vé
     * Khác với booking.customer nếu vé đã được chuyển nhượng
     * - Ban đầu = booking.customer
     * - Được update mỗi khi vé được trao đổi/bán thành công
     * 
     * CONSTRAINT: currentOwner không thể update nếu isCheckedIn = true
     */
    @ManyToOne(optional = false)
    @JoinColumn(name = "current_owner_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_tickets_current_owner_id"))
    private User currentOwner;
    
    /**
     * Flag để kiểm soát vé có thể được trao đổi hay không
     * - true: vé có thể được list/exchange
     * - false: vé không thể bao giờ được chuyển nhượng (VIP lifetime ticket, etc)
     * 
     * CONSTRAINT: Nếu isTransferable = false, TicketListing không thể được tạo
     */
    @Column(name = "is_transferable")
    private Boolean isTransferable;
    
    /**
     * Flag tracking check-in status
     * 
     * CRITICAL CONSTRAINT: Khi isCheckedIn = true:
     * 1. Vé KHÔNG thể được listed lên TicketListing
     * 2. Vé KHÔNG thể được trao đổi qua TicketExchange
     * 3. currentOwner KHÔNG thể được thay đổi
     * 
     * Validation: Phải được check tại Service Layer khi listing/exchange
     */
    @Column(name = "is_checked_in")
    private Boolean isCheckedIn;
    
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;
    
    @ManyToOne
    @JoinColumn(name = "checked_in_by",
                foreignKey = @ForeignKey(name = "fk_tickets_checked_in_by"))
    private User checkedInBy;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (isCheckedIn == null) {
            isCheckedIn = false;
        }
        if (isTransferable == null) {
            isTransferable = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

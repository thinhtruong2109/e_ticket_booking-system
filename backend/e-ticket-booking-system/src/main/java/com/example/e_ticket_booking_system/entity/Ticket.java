package com.example.e_ticket_booking_system.entity;

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
    
    @Column(name = "booking_id", nullable = false)
    private Long bookingId;
    
    @Column(name = "ticket_type_id", nullable = false)
    private Long ticketTypeId;
    
    @Column(name = "qr_code", nullable = false, unique = true, columnDefinition = "TEXT")
    private String qrCode;
    
    @Column(name = "current_owner_id", nullable = false)
    private Long currentOwnerId;
    
    @Column(name = "is_transferable")
    private Boolean isTransferable;
    
    @Column(name = "is_checked_in")
    private Boolean isCheckedIn;
    
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;
    
    @Column(name = "checked_in_by")
    private Long checkedInBy;
    
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

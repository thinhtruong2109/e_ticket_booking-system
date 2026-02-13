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
@Table(name = "seat_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "seat_id", nullable = false)
    private Long seatId;
    
    @Column(name = "event_schedule_id", nullable = false)
    private Long eventScheduleId;
    
    @Column(name = "booking_id")
    private Long bookingId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // HOLDING, CONFIRMED, RELEASED
    
    @Column(name = "hold_expires_at")
    private LocalDateTime holdExpiresAt;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
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

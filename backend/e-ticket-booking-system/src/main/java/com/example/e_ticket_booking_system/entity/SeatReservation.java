package com.example.e_ticket_booking_system.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
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
@Table(name = "seat_reservations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SeatReservation {
    
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "seat_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_seat_reservations_seat_id"))
    private Seat seat;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "event_schedule_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_seat_reservations_event_schedule_id"))
    private EventSchedule eventSchedule;
    
    @ManyToOne
    @JoinColumn(name = "booking_id",
                foreignKey = @ForeignKey(name = "fk_seat_reservations_booking_id"))
    private Booking booking;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_seat_reservations_user_id"))
    private User usergId;
    
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

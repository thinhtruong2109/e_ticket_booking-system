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
@Table(name = "sections")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Section {
    
    @Id
    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_sections_venue_id"))
    private Venue venue;
    
    @Column(name = "venue_id", nullable = false)
    private Long venueId;
    
    @Column(name = "name", nullable = false, length = 100)
    private String name; // VIP Zone, Section A, B, C
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "capacity")
    private Integer capacity;
    
    @Column(name = "has_numbered_seats")
    private Boolean hasNumberedSeats;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (hasNumberedSeats == null) {
            hasNumberedSeats = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

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
@Table(name = "events")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
    
    @Column(name = "category_id", nullable = false)
    private Long categoryId;
    
    @Column(name = "organizer_id", nullable = false)
    private Long organizerId;
    
    @Column(name = "venue_id", nullable = false)
    private Long venueId;
    
    @Column(name = "banner_image_url", length = 500)
    private String bannerImageUrl;
    
    @Column(name = "thumbnail_image_url", length = 500)
    private String thumbnailImageUrl;
    
    @Column(name = "status", nullable = false, length = 20)
    private String status; // DRAFT, PUBLISHED, ONGOING, COMPLETED, CANCELLED
    
    @Column(name = "total_tickets")
    private Integer totalTickets;
    
    @Column(name = "available_tickets")
    private Integer availableTickets;
    
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

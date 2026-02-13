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
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_events_category_id"))
    private EventCategory category;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "organizer_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_events_organizer_id"))
    private User organizer;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "venue_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_events_venue_id"))
    private Venue venue;
    
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
    
    /**
     * Flag kiểm soát secondary market (sàn trao đổi vé)
     * - true: Cho phép người dùng trao đổi/bán vé
     * - false: Vé không thể transfer (event VIP/restricted)
     * 
     * Impact: 
     * - Event.allowTicketExchange = false → Ticket.isTransferable = false
     * - Event.allowTicketExchange = true → Ticket.isTransferable phụ thuộc TicketType setting
     */
    @Column(name = "allow_ticket_exchange")
    private Boolean allowTicketExchange;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (allowTicketExchange == null) {
            allowTicketExchange = true;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

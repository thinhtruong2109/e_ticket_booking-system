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
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ticket_transfer_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketTransferLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "ticket_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ticket_transfer_logs_ticket_id"))
    private Ticket ticket;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "from_user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ticket_transfer_logs_from_user_id"))
    private User fromUser;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "to_user_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_ticket_transfer_logs_to_user_id"))
    private User toUser;
    
    @ManyToOne
    @JoinColumn(name = "ticket_exchange_id",
                foreignKey = @ForeignKey(name = "fk_ticket_transfer_logs_ticket_exchange_id"))
    private TicketExchange ticketExchange;
    
    @Column(name = "transfer_type", nullable = false, length = 20)
    private String transferType; // EXCHANGE, RETURN, UPGRADE
    
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}

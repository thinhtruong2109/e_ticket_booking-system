package com.example.e_ticket_booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.TicketListing;

@Repository
public interface TicketListingRepository extends JpaRepository<TicketListing, Long> {
    List<TicketListing> findBySellerId(Long sellerId);
    List<TicketListing> findByStatus(String status);
    TicketListing findByTicketId(Long ticketId);
    List<TicketListing> findByStatusAndExpiresAtBefore(String status, LocalDateTime time);

    @Query(value = "SELECT tl.* FROM ticket_listings tl JOIN tickets t ON tl.ticket_id = t.id JOIN bookings b ON t.booking_id = b.id WHERE b.event_id = :eventId AND tl.status = :status", nativeQuery = true)
    List<TicketListing> findByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") String status);
}

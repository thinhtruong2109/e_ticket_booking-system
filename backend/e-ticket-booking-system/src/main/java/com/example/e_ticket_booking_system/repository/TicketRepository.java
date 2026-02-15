package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    Ticket findByTicketCode(String ticketCode);
    Ticket findByQrCode(String qrCode);
    List<Ticket> findByBookingId(Long bookingId);
    List<Ticket> findByCurrentOwnerId(Long currentOwnerId);

    @Query("SELECT t FROM Ticket t WHERE t.isCheckedIn = :checkedIn")
    List<Ticket> findByCheckedIn(@Param("checkedIn") Boolean checkedIn);
}

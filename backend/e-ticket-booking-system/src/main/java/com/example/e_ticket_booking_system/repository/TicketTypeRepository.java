package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventId(Long eventId);
    TicketType findByEventIdAndName(Long eventId, String name);
}

package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.TicketType;

@Repository
public interface TicketTypeRepository extends JpaRepository<TicketType, Long> {
    List<TicketType> findByEventId(Long eventId);
    TicketType findByEventIdAndName(Long eventId, String name);

    @Query("SELECT COALESCE(SUM(t.totalQuantity), 0) FROM TicketType t WHERE t.event.id = :eventId")
    int sumTotalQuantityByEventId(@Param("eventId") Long eventId);
}

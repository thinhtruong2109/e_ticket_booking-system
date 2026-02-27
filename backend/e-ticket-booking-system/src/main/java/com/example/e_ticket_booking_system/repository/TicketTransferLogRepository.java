package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.TicketTransferLog;

@Repository
public interface TicketTransferLogRepository extends JpaRepository<TicketTransferLog, Long> {
    List<TicketTransferLog> findByTicketId(Long ticketId);
    List<TicketTransferLog> findByFromUserId(Long fromUserId);
    List<TicketTransferLog> findByToUserId(Long toUserId);
}

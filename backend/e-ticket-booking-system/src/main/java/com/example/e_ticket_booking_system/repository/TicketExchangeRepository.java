package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.TicketExchange;

@Repository
public interface TicketExchangeRepository extends JpaRepository<TicketExchange, Long> {
    List<TicketExchange> findByTicketListingId(Long ticketListingId);
    List<TicketExchange> findBySellerId(Long sellerId);
    List<TicketExchange> findByBuyerId(Long buyerId);
    List<TicketExchange> findByStatus(String status);
}

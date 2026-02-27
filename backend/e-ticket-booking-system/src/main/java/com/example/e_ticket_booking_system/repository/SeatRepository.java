package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Seat;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByVenueId(Long venueId);
    List<Seat> findBySectionId(Long sectionId);
    Seat findByVenueIdAndSeatNumber(Long venueId, String seatNumber);
}

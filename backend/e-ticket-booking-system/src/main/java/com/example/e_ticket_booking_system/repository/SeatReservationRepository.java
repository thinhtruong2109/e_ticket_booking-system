package com.example.e_ticket_booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.SeatReservation;

@Repository
public interface SeatReservationRepository extends JpaRepository<SeatReservation, Long> {
    List<SeatReservation> findBySeatId(Long seatId);
    List<SeatReservation> findByEventScheduleId(Long eventScheduleId);
    List<SeatReservation> findByBookingId(Long bookingId);
    List<SeatReservation> findByUserId(Long userId);
    List<SeatReservation> findByStatus(String status);
    List<SeatReservation> findByEventScheduleIdAndStatus(Long eventScheduleId, String status);
    List<SeatReservation> findByStatusAndHoldExpiresAtBefore(String status, LocalDateTime time);
}

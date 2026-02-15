package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Booking;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByBookingCode(String bookingCode);
    List<Booking> findByCustomerId(Long customerId);
    List<Booking> findByEventId(Long eventId);
    List<Booking> findByStatus(String status);
}

package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.BookingPromoCode;

@Repository
public interface BookingPromoCodeRepository extends JpaRepository<BookingPromoCode, Long> {
    List<BookingPromoCode> findByBookingId(Long bookingId);
    List<BookingPromoCode> findByPromoCodeId(Long promoCodeId);
}

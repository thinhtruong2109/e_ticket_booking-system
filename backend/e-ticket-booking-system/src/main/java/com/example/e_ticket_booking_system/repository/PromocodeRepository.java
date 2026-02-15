package com.example.e_ticket_booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.PromoCode;

@Repository
public interface PromocodeRepository extends JpaRepository<PromoCode, Long> {
    PromoCode findByCode(String code);
    List<PromoCode> findByStatus(String status);
    List<PromoCode> findByValidFromLessThanAndValidToGreaterThan(LocalDateTime from, LocalDateTime to);
}

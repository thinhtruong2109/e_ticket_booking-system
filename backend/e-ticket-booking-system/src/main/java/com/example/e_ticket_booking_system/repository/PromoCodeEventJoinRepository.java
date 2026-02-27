package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.PromoCodeEventJoin;

@Repository
public interface PromoCodeEventJoinRepository extends JpaRepository<PromoCodeEventJoin, Long> {

    List<PromoCodeEventJoin> findByPromoCodeId(Long promoCodeId);

    List<PromoCodeEventJoin> findByEventId(Long eventId);

    void deleteByPromoCodeId(Long promoCodeId);
}

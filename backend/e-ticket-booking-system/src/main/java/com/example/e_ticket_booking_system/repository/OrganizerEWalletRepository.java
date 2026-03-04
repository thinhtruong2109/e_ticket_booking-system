package com.example.e_ticket_booking_system.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.OrganizerEWallet;

@Repository
public interface OrganizerEWalletRepository extends JpaRepository<OrganizerEWallet, Long> {
    Optional<OrganizerEWallet> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}

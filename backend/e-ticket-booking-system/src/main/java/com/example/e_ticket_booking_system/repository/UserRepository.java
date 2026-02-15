package com.example.e_ticket_booking_system.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmail(String email);
    User findByPhoneNumber(String phoneNumber);
    Optional<User> findOptionalByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findByRole(String role);
    List<User> findByStatus(String status);
}

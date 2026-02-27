package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Venue;

@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    List<Venue> findByCity(String city);
    List<Venue> findByCountry(String country);
}

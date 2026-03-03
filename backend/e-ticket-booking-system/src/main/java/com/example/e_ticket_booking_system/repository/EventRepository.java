package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByCategoryId(Long categoryId);
    List<Event> findByVenueId(Long venueId);
    List<Event> findByStatus(String status);
}

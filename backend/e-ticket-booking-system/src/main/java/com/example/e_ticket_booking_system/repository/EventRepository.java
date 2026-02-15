package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByOrganizerId(Long organizerId);
    List<Event> findByCategoryId(Long categoryId);
    List<Event> findByVenueId(Long venueId);
    List<Event> findByStatus(String status);
    
    @Query("SELECT e FROM Event e WHERE e.status = :status AND " +
           "(:categoryId IS NULL OR e.category.id = :categoryId) AND " +
           "(:name IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :name, '%')))")
    List<Event> searchEvents(@Param("status") String status,
                             @Param("categoryId") Long categoryId,
                             @Param("name") String name);
}

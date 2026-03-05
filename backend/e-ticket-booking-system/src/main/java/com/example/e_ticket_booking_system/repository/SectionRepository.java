package com.example.e_ticket_booking_system.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.Section;

@Repository
public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByVenueId(Long venueId);
    Section findByVenueIdAndName(Long venueId, String name);

    @Query("SELECT COALESCE(SUM(s.capacity), 0) FROM Section s WHERE s.venue.id = :venueId")
    int sumCapacityByVenueId(@Param("venueId") Long venueId);
}

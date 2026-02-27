package com.example.e_ticket_booking_system.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.e_ticket_booking_system.entity.EventSchedule;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {
    List<EventSchedule> findByEventId(Long eventId);
    List<EventSchedule> findByStatus(String status);
    List<EventSchedule> findByStartTimeGreaterThan(LocalDateTime startTime);
}

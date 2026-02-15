package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateEventScheduleRequest;
import com.example.e_ticket_booking_system.dto.response.EventScheduleResponse;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.EventSchedule;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.EventRepository;
import com.example.e_ticket_booking_system.repository.EventScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventScheduleService {

    private static final Logger log = LoggerFactory.getLogger(EventScheduleService.class);

    private final EventScheduleRepository scheduleRepository;
    private final EventRepository eventRepository;

    public EventScheduleResponse createSchedule(Long organizerId, CreateEventScheduleRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.getEventId()));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to add schedules to this event");
        }

        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        EventSchedule schedule = new EventSchedule();
        schedule.setEvent(event);
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setTotalSeats(event.getVenue().getTotalCapacity());
        schedule.setAvailableSeats(event.getVenue().getTotalCapacity());
        schedule.setStatus("SCHEDULED");

        schedule = scheduleRepository.save(schedule);
        log.info("Schedule created for event: {}", event.getName());
        return toResponse(schedule);
    }

    public List<EventScheduleResponse> getSchedulesByEvent(Long eventId) {
        return scheduleRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<EventScheduleResponse> getAvailableSchedules(Long eventId) {
        return scheduleRepository.findByEventId(eventId).stream()
                .filter(s -> "SCHEDULED".equals(s.getStatus()) && 
                             (s.getAvailableSeats() == null || s.getAvailableSeats() > 0))
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventScheduleResponse getScheduleById(Long id) {
        EventSchedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + id));
        return toResponse(schedule);
    }

    public EventScheduleResponse cancelSchedule(Long scheduleId, Long organizerId) {
        EventSchedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ResourceNotFoundException("Schedule not found with id: " + scheduleId));

        if (!schedule.getEvent().getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to cancel this schedule");
        }

        schedule.setStatus("CANCELLED");
        schedule = scheduleRepository.save(schedule);
        log.info("Schedule cancelled for event: {}", schedule.getEvent().getName());
        return toResponse(schedule);
    }

    private EventScheduleResponse toResponse(EventSchedule schedule) {
        return new EventScheduleResponse(
                schedule.getId(), schedule.getEvent().getId(),
                schedule.getEvent().getName(),
                schedule.getStartTime(), schedule.getEndTime(),
                schedule.getTotalSeats(), schedule.getAvailableSeats(),
                schedule.getStatus());
    }
}

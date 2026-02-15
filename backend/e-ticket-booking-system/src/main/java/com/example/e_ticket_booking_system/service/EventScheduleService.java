package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        // Tìm event theo ID
        Optional<Event> optionalEvent = eventRepository.findById(request.getEventId());
        if (!optionalEvent.isPresent()) {
            throw new ResourceNotFoundException("Event not found with id: " + request.getEventId());
        }
        Event event = optionalEvent.get();

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
        List<EventSchedule> schedules = scheduleRepository.findByEventId(eventId);
        // Chuyển từ danh sách EventSchedule sang danh sách EventScheduleResponse
        List<EventScheduleResponse> responseList = new ArrayList<>();
        for (EventSchedule schedule : schedules) {
            EventScheduleResponse response = toResponse(schedule);
            responseList.add(response);
        }
        return responseList;
    }

    public List<EventScheduleResponse> getAvailableSchedules(Long eventId) {
        List<EventSchedule> allSchedules = scheduleRepository.findByEventId(eventId);
        // Lọc ra các schedule có status SCHEDULED và còn chỗ
        List<EventScheduleResponse> responseList = new ArrayList<>();
        for (EventSchedule s : allSchedules) {
            boolean isScheduled = "SCHEDULED".equals(s.getStatus());
            boolean hasAvailableSeats = (s.getAvailableSeats() == null || s.getAvailableSeats() > 0);
            if (isScheduled && hasAvailableSeats) {
                EventScheduleResponse response = toResponse(s);
                responseList.add(response);
            }
        }
        return responseList;
    }

    public EventScheduleResponse getScheduleById(Long id) {
        Optional<EventSchedule> optionalSchedule = scheduleRepository.findById(id);
        if (!optionalSchedule.isPresent()) {
            throw new ResourceNotFoundException("Schedule not found with id: " + id);
        }
        EventSchedule schedule = optionalSchedule.get();
        return toResponse(schedule);
    }

    public EventScheduleResponse cancelSchedule(Long scheduleId, Long organizerId) {
        Optional<EventSchedule> optionalSchedule = scheduleRepository.findById(scheduleId);
        if (!optionalSchedule.isPresent()) {
            throw new ResourceNotFoundException("Schedule not found with id: " + scheduleId);
        }
        EventSchedule schedule = optionalSchedule.get();

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

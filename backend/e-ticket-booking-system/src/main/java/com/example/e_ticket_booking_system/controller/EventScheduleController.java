package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.CreateEventScheduleRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.EventScheduleResponse;
import com.example.e_ticket_booking_system.service.EventScheduleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/event-schedules")
@RequiredArgsConstructor
public class EventScheduleController {

    private final EventScheduleService scheduleService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<EventScheduleResponse>>> getByEvent(@RequestParam Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getSchedulesByEvent(eventId)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<EventScheduleResponse>>> getAvailable(@RequestParam Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getAvailableSchedules(eventId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventScheduleResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(scheduleService.getScheduleById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventScheduleResponse>> createSchedule(
            @Valid @RequestBody CreateEventScheduleRequest request) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Schedule created",
                scheduleService.createSchedule(organizerId, request)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventScheduleResponse>> cancelSchedule(@PathVariable Long id) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Schedule cancelled",
                scheduleService.cancelSchedule(id, organizerId)));
    }
}

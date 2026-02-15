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
import com.example.e_ticket_booking_system.dto.request.CreateEventRequest;
import com.example.e_ticket_booking_system.dto.request.UpdateEventRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.EventResponse;
import com.example.e_ticket_booking_system.service.EventService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final SecurityUtils securityUtils;

    // Public endpoints
    @GetMapping
    public ResponseEntity<ApiResponse<List<EventResponse>>> getPublishedEvents(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String name) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getPublishedEvents(categoryId, name)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EventResponse>> getEventById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventById(id)));
    }

    // Organizer endpoints
    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> createEvent(
            @Valid @RequestBody CreateEventRequest request) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Event created", eventService.createEvent(organizerId, request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> updateEvent(
            @PathVariable Long id, @RequestBody UpdateEventRequest request) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Event updated", eventService.updateEvent(id, organizerId, request)));
    }

    @PutMapping("/{id}/publish")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> publishEvent(@PathVariable Long id) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Event published", eventService.publishEvent(id, organizerId)));
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<EventResponse>> cancelEvent(@PathVariable Long id) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Event cancelled", eventService.cancelEvent(id, organizerId)));
    }

    @GetMapping("/my-events")
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getMyEvents() {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(eventService.getEventsByOrganizer(organizerId)));
    }

    // Admin
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<EventResponse>>> getAllEvents() {
        return ResponseEntity.ok(ApiResponse.success(eventService.getAllEvents()));
    }
}

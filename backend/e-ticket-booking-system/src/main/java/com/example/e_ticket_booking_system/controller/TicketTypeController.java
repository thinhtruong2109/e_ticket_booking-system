package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.CreateTicketTypeRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.TicketTypeResponse;
import com.example.e_ticket_booking_system.service.TicketTypeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ticket-types")
@RequiredArgsConstructor
public class TicketTypeController {

    private final TicketTypeService ticketTypeService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TicketTypeResponse>>> getByEvent(@RequestParam Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.getTicketTypesByEvent(eventId)));
    }

    @GetMapping("/available")
    public ResponseEntity<ApiResponse<List<TicketTypeResponse>>> getAvailable(@RequestParam Long eventId) {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.getAvailableTicketTypes(eventId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(ticketTypeService.getTicketTypeById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ORGANIZER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TicketTypeResponse>> createTicketType(
            @Valid @RequestBody CreateTicketTypeRequest request) {
        Long organizerId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success("Ticket type created",
                ticketTypeService.createTicketType(organizerId, request)));
    }
}

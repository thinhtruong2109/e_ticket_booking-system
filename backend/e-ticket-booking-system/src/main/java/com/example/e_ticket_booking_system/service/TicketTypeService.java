package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateTicketTypeRequest;
import com.example.e_ticket_booking_system.dto.response.TicketTypeResponse;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.TicketType;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.EventRepository;
import com.example.e_ticket_booking_system.repository.TicketTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketTypeService {

    private static final Logger log = LoggerFactory.getLogger(TicketTypeService.class);

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;

    public TicketTypeResponse createTicketType(Long organizerId, CreateTicketTypeRequest request) {
        Event event = eventRepository.findById(request.getEventId())
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + request.getEventId()));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to add ticket types to this event");
        }

        if (ticketTypeRepository.findByEventIdAndName(request.getEventId(), request.getName()) != null) {
            throw new BadRequestException("Ticket type with name '" + request.getName() + "' already exists for this event");
        }

        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);
        ticketType.setName(request.getName());
        ticketType.setDescription(request.getDescription());
        ticketType.setPrice(request.getPrice());
        ticketType.setTotalQuantity(request.getTotalQuantity());
        ticketType.setAvailableQuantity(request.getTotalQuantity());
        ticketType.setMaxPerBooking(request.getMaxPerBooking() != null ? request.getMaxPerBooking() : 10);

        ticketType = ticketTypeRepository.save(ticketType);
        log.info("Ticket type created: {} for event: {}", ticketType.getName(), event.getName());
        return toResponse(ticketType);
    }

    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<TicketTypeResponse> getAvailableTicketTypes(Long eventId) {
        return ticketTypeRepository.findByEventId(eventId).stream()
                .filter(tt -> tt.getAvailableQuantity() > 0)
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public TicketTypeResponse getTicketTypeById(Long id) {
        TicketType ticketType = ticketTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket type not found with id: " + id));
        return toResponse(ticketType);
    }

    private TicketTypeResponse toResponse(TicketType tt) {
        return new TicketTypeResponse(
                tt.getId(), tt.getEvent().getId(), tt.getName(),
                tt.getDescription(), tt.getPrice(), tt.getTotalQuantity(),
                tt.getAvailableQuantity(), tt.getMaxPerBooking());
    }
}

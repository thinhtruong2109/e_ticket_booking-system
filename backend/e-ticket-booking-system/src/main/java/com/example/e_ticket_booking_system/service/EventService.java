package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateEventRequest;
import com.example.e_ticket_booking_system.dto.request.UpdateEventRequest;
import com.example.e_ticket_booking_system.dto.response.EventCategoryResponse;
import com.example.e_ticket_booking_system.dto.response.EventResponse;
import com.example.e_ticket_booking_system.dto.response.UserResponse;
import com.example.e_ticket_booking_system.dto.response.VenueResponse;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.EventCategory;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.entity.Venue;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.BookingRepository;
import com.example.e_ticket_booking_system.repository.EventCategoryRepository;
import com.example.e_ticket_booking_system.repository.EventRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;
import com.example.e_ticket_booking_system.repository.VenueRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public EventResponse createEvent(Long organizerId, CreateEventRequest request) {
        User organizer = userRepository.findById(organizerId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + organizerId));

        EventCategory category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue not found with id: " + request.getVenueId()));

        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setThumbnailImageUrl(request.getThumbnailImageUrl());
        event.setStatus("DRAFT");
        event.setTotalTickets(request.getTotalTickets() != null ? request.getTotalTickets() : 
                             venue.getTotalCapacity() != null ? venue.getTotalCapacity() : 0);
        event.setAvailableTickets(event.getTotalTickets());
        event.setAllowTicketExchange(request.getAllowTicketExchange() != null ? request.getAllowTicketExchange() : true);

        event = eventRepository.save(event);
        log.info("Event created: {} by organizer: {}", event.getName(), organizer.getEmail());
        return toResponse(event);
    }

    public EventResponse publishEvent(Long eventId, Long organizerId) {
        Event event = getEventAndCheckOwner(eventId, organizerId);

        if (!"DRAFT".equals(event.getStatus())) {
            throw new BadRequestException("Only DRAFT events can be published");
        }

        event.setStatus("PUBLISHED");
        event = eventRepository.save(event);
        log.info("Event published: {}", event.getName());
        return toResponse(event);
    }

    public EventResponse cancelEvent(Long eventId, Long organizerId) {
        Event event = getEventAndCheckOwner(eventId, organizerId);
        event.setStatus("CANCELLED");
        event = eventRepository.save(event);
        log.info("Event cancelled: {}", event.getName());
        return toResponse(event);
    }

    public List<EventResponse> getPublishedEvents(Long categoryId, String name) {
        return eventRepository.searchEvents("PUBLISHED", categoryId, name).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventResponse getEventById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + id));
        return toResponse(event);
    }

    public List<EventResponse> getEventsByOrganizer(Long organizerId) {
        return eventRepository.findByOrganizerId(organizerId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EventResponse updateEvent(Long eventId, Long organizerId, UpdateEventRequest request) {
        Event event = getEventAndCheckOwner(eventId, organizerId);

        // If bookings exist, restrict certain changes
        boolean hasBookings = !bookingRepository.findByEventId(eventId).isEmpty();

        if (request.getName() != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategoryId() != null) {
            EventCategory category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
            event.setCategory(category);
        }
        if (request.getBannerImageUrl() != null) event.setBannerImageUrl(request.getBannerImageUrl());
        if (request.getThumbnailImageUrl() != null) event.setThumbnailImageUrl(request.getThumbnailImageUrl());
        if (request.getAllowTicketExchange() != null) event.setAllowTicketExchange(request.getAllowTicketExchange());

        event = eventRepository.save(event);
        log.info("Event updated: {}", event.getName());
        return toResponse(event);
    }

    // Admin methods
    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private Event getEventAndCheckOwner(Long eventId, Long organizerId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found with id: " + eventId));

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to modify this event");
        }
        return event;
    }

    private EventResponse toResponse(Event event) {
        EventResponse response = new EventResponse();
        response.setId(event.getId());
        response.setName(event.getName());
        response.setDescription(event.getDescription());
        response.setCategory(new EventCategoryResponse(
                event.getCategory().getId(), event.getCategory().getName(),
                event.getCategory().getDescription(), event.getCategory().getIconUrl(),
                event.getCategory().getCreatedAt()));
        response.setOrganizer(new UserResponse(
                event.getOrganizer().getId(), event.getOrganizer().getEmail(),
                event.getOrganizer().getFullName(), event.getOrganizer().getPhoneNumber(),
                event.getOrganizer().getRole(), event.getOrganizer().getStatus(),
                event.getOrganizer().getCreatedAt()));
        response.setVenue(new VenueResponse(
                event.getVenue().getId(), event.getVenue().getName(),
                event.getVenue().getAddress(), event.getVenue().getCity(),
                event.getVenue().getCountry(), event.getVenue().getTotalCapacity(),
                event.getVenue().getHasSeatMap(), event.getVenue().getCreatedAt()));
        response.setBannerImageUrl(event.getBannerImageUrl());
        response.setThumbnailImageUrl(event.getThumbnailImageUrl());
        response.setStatus(event.getStatus());
        response.setTotalTickets(event.getTotalTickets());
        response.setAvailableTickets(event.getAvailableTickets());
        response.setAllowTicketExchange(event.getAllowTicketExchange());
        response.setCreatedAt(event.getCreatedAt());
        response.setUpdatedAt(event.getUpdatedAt());
        return response;
    }
}

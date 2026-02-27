package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        // Tìm organizer theo ID
        Optional<User> optionalOrganizer = userRepository.findById(organizerId);
        if (!optionalOrganizer.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + organizerId);
        }
        User organizer = optionalOrganizer.get();

        // Tìm category theo ID
        Optional<EventCategory> optionalCategory = categoryRepository.findById(request.getCategoryId());
        if (!optionalCategory.isPresent()) {
            throw new ResourceNotFoundException("Category not found with id: " + request.getCategoryId());
        }
        EventCategory category = optionalCategory.get();

        // Tìm venue theo ID
        Optional<Venue> optionalVenue = venueRepository.findById(request.getVenueId());
        if (!optionalVenue.isPresent()) {
            throw new ResourceNotFoundException("Venue not found with id: " + request.getVenueId());
        }
        Venue venue = optionalVenue.get();

        Event event = new Event();
        event.setName(request.getName());
        event.setDescription(request.getDescription());
        event.setCategory(category);
        event.setOrganizer(organizer);
        event.setVenue(venue);
        event.setBannerImageUrl(request.getBannerImageUrl());
        event.setThumbnailImageUrl(request.getThumbnailImageUrl());
        event.setStatus("DRAFT");

        // Xác định totalTickets: ưu tiên từ request, nếu không thì lấy từ venue capacity
        int totalTickets = 0;
        if (request.getTotalTickets() != null) {
            totalTickets = request.getTotalTickets();
        } else if (venue.getTotalCapacity() != null) {
            totalTickets = venue.getTotalCapacity();
        }
        event.setTotalTickets(totalTickets);
        event.setAvailableTickets(totalTickets);

        // Xác định allowTicketExchange
        if (request.getAllowTicketExchange() != null) {
            event.setAllowTicketExchange(request.getAllowTicketExchange());
        } else {
            event.setAllowTicketExchange(true);
        }

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
        List<Event> events = eventRepository.searchEvents("PUBLISHED", categoryId, name);
        // Chuyển từ danh sách Event sang danh sách EventResponse
        List<EventResponse> responseList = new ArrayList<>();
        for (Event event : events) {
            EventResponse response = toResponse(event);
            responseList.add(response);
        }
        return responseList;
    }

    public EventResponse getEventById(Long id) {
        Optional<Event> optionalEvent = eventRepository.findById(id);
        if (!optionalEvent.isPresent()) {
            throw new ResourceNotFoundException("Event not found with id: " + id);
        }
        Event event = optionalEvent.get();
        return toResponse(event);
    }

    public List<EventResponse> getEventsByOrganizer(Long organizerId) {
        List<Event> events = eventRepository.findByOrganizerId(organizerId);
        List<EventResponse> responseList = new ArrayList<>();
        for (Event event : events) {
            EventResponse response = toResponse(event);
            responseList.add(response);
        }
        return responseList;
    }

    public EventResponse updateEvent(Long eventId, Long organizerId, UpdateEventRequest request) {
        Event event = getEventAndCheckOwner(eventId, organizerId);

        // If bookings exist, restrict certain changes
        boolean hasBookings = !bookingRepository.findByEventId(eventId).isEmpty();

        if (request.getName() != null) event.setName(request.getName());
        if (request.getDescription() != null) event.setDescription(request.getDescription());
        if (request.getCategoryId() != null) {
            Optional<EventCategory> optionalCat = categoryRepository.findById(request.getCategoryId());
            if (!optionalCat.isPresent()) {
                throw new ResourceNotFoundException("Category not found");
            }
            EventCategory category = optionalCat.get();
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
        List<Event> events = eventRepository.findAll();
        List<EventResponse> responseList = new ArrayList<>();
        for (Event event : events) {
            EventResponse response = toResponse(event);
            responseList.add(response);
        }
        return responseList;
    }

    private Event getEventAndCheckOwner(Long eventId, Long organizerId) {
        Optional<Event> optionalEvent = eventRepository.findById(eventId);
        if (!optionalEvent.isPresent()) {
            throw new ResourceNotFoundException("Event not found with id: " + eventId);
        }
        Event event = optionalEvent.get();

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

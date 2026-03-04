package com.example.e_ticket_booking_system.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateEventRequest;
import com.example.e_ticket_booking_system.dto.request.UpdateEventRequest;
import com.example.e_ticket_booking_system.dto.response.EventCategoryResponse;
import com.example.e_ticket_booking_system.dto.response.EventResponse;
import com.example.e_ticket_booking_system.dto.response.UserResponse;
import com.example.e_ticket_booking_system.dto.response.VenueResponse;
import com.example.e_ticket_booking_system.entity.Booking;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.EventCategory;
import com.example.e_ticket_booking_system.entity.EventSchedule;
import com.example.e_ticket_booking_system.entity.Ticket;
import com.example.e_ticket_booking_system.entity.TicketListing;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.entity.Venue;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.BookingRepository;
import com.example.e_ticket_booking_system.repository.EventCategoryRepository;
import com.example.e_ticket_booking_system.repository.EventRepository;
import com.example.e_ticket_booking_system.repository.EventScheduleRepository;
import com.example.e_ticket_booking_system.repository.TicketListingRepository;
import com.example.e_ticket_booking_system.repository.TicketRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;
import com.example.e_ticket_booking_system.repository.VenueRepository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EventService {

    private static final Logger log = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final EventCategoryRepository categoryRepository;
    private final EventScheduleRepository scheduleRepository;
    private final VenueRepository venueRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final TicketRepository ticketRepository;
    private final TicketListingRepository ticketListingRepository;
    private final EmailService emailService;
    private final EntityManager entityManager;

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
        StringBuilder sql = new StringBuilder("SELECT * FROM events WHERE status = 'PUBLISHED'");

        if (categoryId != null) {
            sql.append(" AND category_id = :categoryId");
        }
        if (name != null) {
            sql.append(" AND LOWER(name) LIKE :name");
        }

        var query = entityManager.createNativeQuery(sql.toString(), Event.class);

        if (categoryId != null) {
            query.setParameter("categoryId", categoryId);
        }
        if (name != null) {
            query.setParameter("name", "%" + name.toLowerCase() + "%");
        }

        List<Event> events = query.getResultList();
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

    @Transactional
    public EventResponse updateEvent(Long eventId, Long organizerId, UpdateEventRequest request) {
        Event event = getEventAndCheckOwner(eventId, organizerId);

        // ============================================================
        // 1. Thu thập các thay đổi để track & thông báo cho customers
        // ============================================================
        List<String> changes = new ArrayList<>();
        boolean hasActiveBookings = false;

        // Lấy danh sách bookings active (PENDING hoặc CONFIRMED) của event
        List<Booking> allBookings = bookingRepository.findByEventId(eventId);
        List<Booking> activeBookings = new ArrayList<>();
        for (Booking b : allBookings) {
            if ("PENDING".equals(b.getStatus()) || "CONFIRMED".equals(b.getStatus())) {
                activeBookings.add(b);
            }
        }
        hasActiveBookings = !activeBookings.isEmpty();

        // ============================================================
        // 2. Cập nhật từng trường và track thay đổi
        // ============================================================

        // --- Tên event ---
        if (request.getName() != null && !request.getName().equals(event.getName())) {
            String oldName = event.getName();
            event.setName(request.getName());
            changes.add("Event name changed from \"" + oldName + "\" to \"" + request.getName() + "\"");
        }

        // --- Mô tả ---
        if (request.getDescription() != null && !request.getDescription().equals(event.getDescription())) {
            event.setDescription(request.getDescription());
            changes.add("Event description has been updated");
        }

        // --- Category ---
        if (request.getCategoryId() != null && !request.getCategoryId().equals(event.getCategory().getId())) {
            Optional<EventCategory> optionalCat = categoryRepository.findById(request.getCategoryId());
            if (!optionalCat.isPresent()) {
                throw new ResourceNotFoundException("Category not found");
            }
            EventCategory newCategory = optionalCat.get();
            String oldCategoryName = event.getCategory().getName();
            event.setCategory(newCategory);
            changes.add("Event category changed from \"" + oldCategoryName + "\" to \"" + newCategory.getName() + "\"");
        }

        // --- Venue (địa điểm) ---
        if (request.getVenueId() != null && !request.getVenueId().equals(event.getVenue().getId())) {
            Optional<Venue> optionalVenue = venueRepository.findById(request.getVenueId());
            if (!optionalVenue.isPresent()) {
                throw new ResourceNotFoundException("Venue not found with id: " + request.getVenueId());
            }
            Venue newVenue = optionalVenue.get();
            String oldVenueInfo = event.getVenue().getName() + " (" + event.getVenue().getAddress() + ")";
            String newVenueInfo = newVenue.getName() + " (" + newVenue.getAddress() + ")";
            event.setVenue(newVenue);
            changes.add("Event venue changed from \"" + oldVenueInfo + "\" to \"" + newVenueInfo + "\"");
        }

        // --- Banner/Thumbnail ---
        if (request.getBannerImageUrl() != null) {
            event.setBannerImageUrl(request.getBannerImageUrl());
        }
        if (request.getThumbnailImageUrl() != null) {
            event.setThumbnailImageUrl(request.getThumbnailImageUrl());
        }

        // ============================================================
        // 3. Xử lí allowTicketExchange - cascade tới Tickets & Listings
        // ============================================================
        if (request.getAllowTicketExchange() != null
                && !request.getAllowTicketExchange().equals(event.getAllowTicketExchange())) {

            Boolean newValue = request.getAllowTicketExchange();
            event.setAllowTicketExchange(newValue);

            if (Boolean.FALSE.equals(newValue)) {
                // === TẮT trao đổi vé ===
                // 3a. Cập nhật tất cả tickets của event: transferable = false
                // Lấy tất cả tickets của event thông qua bookings
                List<Booking> eventBookings = bookingRepository.findByEventId(eventId);
                List<Ticket> eventTickets = new ArrayList<>();
                for (Booking b : eventBookings) {
                    eventTickets.addAll(ticketRepository.findByBookingId(b.getId()));
                }
                for (Ticket ticket : eventTickets) {
                    ticket.setTransferable(false);
                    ticketRepository.save(ticket);
                }
                log.info("Disabled transferable for {} tickets of event: {}", eventTickets.size(), event.getName());

                // 3b. Hủy tất cả TicketListings đang active (FOR_SALE)
                List<TicketListing> activeListings = new ArrayList<>();
                for (Ticket t : eventTickets) {
                    TicketListing listing = ticketListingRepository.findByTicketId(t.getId());
                    if (listing != null && "FOR_SALE".equals(listing.getStatus())) {
                        activeListings.add(listing);
                    }
                }
                for (TicketListing listing : activeListings) {
                    listing.setStatus("CANCELLED");
                    ticketListingRepository.save(listing);
                }
                if (!activeListings.isEmpty()) {
                    log.info("Cancelled {} active ticket listings for event: {}", activeListings.size(), event.getName());
                }

                changes.add("Ticket exchange has been DISABLED - All active ticket listings have been cancelled. Tickets can no longer be transferred or resold");

            } else {
                // === BẬT lại trao đổi vé ===
                // Cập nhật tickets: transferable = true (cho phép lại)
                List<Booking> eventBookings2 = bookingRepository.findByEventId(eventId);
                List<Ticket> eventTickets = new ArrayList<>();
                for (Booking b : eventBookings2) {
                    eventTickets.addAll(ticketRepository.findByBookingId(b.getId()));
                }
                for (Ticket ticket : eventTickets) {
                    ticket.setTransferable(true);
                    ticketRepository.save(ticket);
                }
                log.info("Enabled transferable for {} tickets of event: {}", eventTickets.size(), event.getName());

                changes.add("Ticket exchange has been ENABLED - Tickets can now be transferred and resold on the marketplace");
            }
        }

        // ============================================================
        // 4. Lưu event đã cập nhật
        // ============================================================
        event = eventRepository.save(event);
        log.info("Event updated: {}", event.getName());

        // ============================================================
        // 5. Gửi thông báo email cho tất cả customers có booking active
        //    (chỉ khi có thay đổi quan trọng VÀ có booking active)
        // ============================================================
        if (hasActiveBookings && !changes.isEmpty()) {
            notifyBookedCustomers(event, activeBookings, changes);
        }

        return toResponse(event);
    }

    /**
     * Gửi email thông báo cập nhật event cho tất cả customers đang có booking active.
     * Dùng Set<email> để tránh gửi trùng nếu 1 customer có nhiều bookings.
     */
    private void notifyBookedCustomers(Event event, List<Booking> activeBookings, List<String> changes) {
        // Build nội dung HTML cho phần thay đổi
        StringBuilder changesHtml = new StringBuilder();
        for (String change : changes) {
            changesHtml.append("<p style=\"margin: 0 0 8px; color: #111827; font-size: 14px;\">")
                       .append("&#8226; ").append(change)
                       .append("</p>");
        }

        // Track email đã gửi để tránh duplicate
        Set<String> notifiedEmails = new HashSet<>();

        for (Booking booking : activeBookings) {
            User customer = booking.getCustomer();
            String email = customer.getEmail();

            // Gửi email cho mỗi booking (1 customer có thể có nhiều booking → gửi mỗi booking 1 email)
            try {
                emailService.sendEventUpdateNotification(
                        email,
                        customer.getFullName(),
                        event.getName(),
                        booking.getBookingCode(),
                        changesHtml.toString()
                );
                notifiedEmails.add(email);
                log.info("Sent event update notification to {} for booking {}", email, booking.getBookingCode());
            } catch (Exception e) {
                // Không throw exception nếu gửi email fail → đảm bảo update event vẫn thành công
                log.error("Failed to send event update notification to {} for booking {}: {}",
                        email, booking.getBookingCode(), e.getMessage());
            }
        }

        log.info("Event update notifications sent to {} unique customers for event: {}",
                notifiedEmails.size(), event.getName());
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

    /**
     * Scheduled task: Update event and schedule statuses based on time.
     * - EventSchedule: SCHEDULED → ONGOING (startTime reached)
     * - EventSchedule: ONGOING → COMPLETED (endTime passed)
     * - Event: PUBLISHED → ONGOING (if any schedule is ONGOING)
     * - Event: ONGOING → COMPLETED (if all schedules are COMPLETED/CANCELLED)
     */
    @Transactional
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Update schedules: SCHEDULED → ONGOING (startTime đã đến)
        List<EventSchedule> schedulesToStart = scheduleRepository
                .findByStatusAndStartTimeBefore("SCHEDULED", now);
        for (EventSchedule schedule : schedulesToStart) {
            schedule.setStatus("ONGOING");
            scheduleRepository.save(schedule);
            log.info("Schedule {} started for event: {}", schedule.getId(), schedule.getEvent().getName());
        }

        // 2. Update schedules: ONGOING → COMPLETED (endTime đã qua)
        List<EventSchedule> schedulesToComplete = scheduleRepository
                .findByStatusAndEndTimeBefore("ONGOING", now);
        for (EventSchedule schedule : schedulesToComplete) {
            schedule.setStatus("COMPLETED");
            scheduleRepository.save(schedule);
            log.info("Schedule {} completed for event: {}", schedule.getId(), schedule.getEvent().getName());
        }

        // 3. Update events: PUBLISHED → ONGOING (nếu có schedule đang ONGOING)
        List<Event> publishedEvents = eventRepository.findByStatus("PUBLISHED");
        for (Event event : publishedEvents) {
            List<EventSchedule> ongoingSchedules = scheduleRepository
                    .findByEventIdAndStatus(event.getId(), "ONGOING");
            if (!ongoingSchedules.isEmpty()) {
                event.setStatus("ONGOING");
                eventRepository.save(event);
                log.info("Event status updated to ONGOING: {}", event.getName());
            }
        }

        // 4. Update events: ONGOING → COMPLETED (nếu tất cả schedule đã COMPLETED/CANCELLED)
        List<Event> ongoingEvents = eventRepository.findByStatus("ONGOING");
        for (Event event : ongoingEvents) {
            List<EventSchedule> schedules = scheduleRepository.findByEventId(event.getId());
            if (schedules.isEmpty()) continue;

            boolean allDone = true;
            for (EventSchedule schedule : schedules) {
                if (!"COMPLETED".equals(schedule.getStatus()) && !"CANCELLED".equals(schedule.getStatus())) {
                    allDone = false;
                    break;
                }
            }
            if (allDone) {
                event.setStatus("COMPLETED");
                eventRepository.save(event);
                log.info("Event status updated to COMPLETED: {}", event.getName());
            }
        }
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

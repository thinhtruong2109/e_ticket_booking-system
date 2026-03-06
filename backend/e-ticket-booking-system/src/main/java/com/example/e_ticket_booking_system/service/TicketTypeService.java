
package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.CreateTicketTypeRequest;
import com.example.e_ticket_booking_system.dto.request.UpdateTicketTypeRequest;
import com.example.e_ticket_booking_system.dto.response.TicketTypeResponse;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.Section;
import com.example.e_ticket_booking_system.entity.TicketType;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.EventRepository;
import com.example.e_ticket_booking_system.repository.SeatRepository;
import com.example.e_ticket_booking_system.repository.SectionRepository;
import com.example.e_ticket_booking_system.repository.TicketTypeRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TicketTypeService {
    public TicketTypeResponse updateTicketType(Long organizerId, UpdateTicketTypeRequest request) {
        Optional<TicketType> optionalTicketType = ticketTypeRepository.findById(request.getId());
        if (!optionalTicketType.isPresent()) {
            throw new ResourceNotFoundException("Ticket type not found with id: " + request.getId());
        }
        TicketType ticketType = optionalTicketType.get();
        Event event = ticketType.getEvent();
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to update ticket types for this event");
        }
        // Validate name uniqueness (except self)
        TicketType existing = ticketTypeRepository.findByEventIdAndName(request.getEventId(), request.getName());
        if (existing != null && !existing.getId().equals(ticketType.getId())) {
            throw new BadRequestException("Ticket type with name '" + request.getName() + "' already exists for this event");
        }
        // Validate: sum of all ticket types' totalQuantity (except self) + new must not exceed event.totalTickets
        int currentTotalQuantity = ticketTypeRepository.sumTotalQuantityByEventId(request.getEventId()) - ticketType.getTotalQuantity();
        if (event.getTotalTickets() > 0 && currentTotalQuantity + request.getTotalQuantity() > event.getTotalTickets()) {
            throw new BadRequestException(
                    "Total ticket type quantity (" + (currentTotalQuantity + request.getTotalQuantity())
                            + ") would exceed event total tickets (" + event.getTotalTickets()
                            + "). Current ticket types use " + currentTotalQuantity + " tickets");
        }
        // Gắn section nếu có
        if (request.getSectionId() != null) {
            Optional<Section> optionalSection = sectionRepository.findById(request.getSectionId());
            if (!optionalSection.isPresent()) {
                throw new ResourceNotFoundException("Section not found with id: " + request.getSectionId());
            }
            Section section = optionalSection.get();
            if (section.getCapacity() != null && request.getTotalQuantity() > section.getCapacity()) {
                throw new BadRequestException(
                        "Total quantity (" + request.getTotalQuantity()
                                + ") exceeds section '" + section.getName()
                                + "' capacity (" + section.getCapacity() + ")");
            }
            if (Boolean.TRUE.equals(section.getHasNumberedSeats())) {
                long seatCount = seatRepository.countBySectionId(section.getId());
                if (seatCount == 0) {
                    throw new BadRequestException(
                            "Section '" + section.getName()
                                    + "' has numbered seats enabled but no seats have been created yet. "
                                    + "Please create seats first before updating ticket types");
                }
                if (request.getTotalQuantity() > seatCount) {
                    throw new BadRequestException(
                            "Total quantity (" + request.getTotalQuantity()
                                    + ") exceeds the number of actual seats (" + seatCount
                                    + ") in section '" + section.getName()
                                    + "'. For numbered seat sections, ticket quantity cannot exceed seat count");
                }
            }
            ticketType.setSection(section);
        } else {
            ticketType.setSection(null);
        }
        ticketType.setName(request.getName());
        ticketType.setDescription(request.getDescription());
        ticketType.setPrice(request.getPrice());
        // Nếu tăng tổng số lượng, tăng availableQuantity tương ứng
        int diff = request.getTotalQuantity() - ticketType.getTotalQuantity();
        ticketType.setTotalQuantity(request.getTotalQuantity());
        ticketType.setAvailableQuantity(ticketType.getAvailableQuantity() + diff);
        ticketType.setMaxPerBooking(Objects.requireNonNullElse(request.getMaxPerBooking(), 10));
        ticketType = ticketTypeRepository.save(ticketType);
        log.info("Ticket type updated: {} for event: {}", ticketType.getName(), event.getName());
        return toResponse(ticketType);
    }

    public void deleteTicketType(Long organizerId, Long ticketTypeId) {
        Optional<TicketType> optionalTicketType = ticketTypeRepository.findById(ticketTypeId);
        if (!optionalTicketType.isPresent()) {
            throw new ResourceNotFoundException("Ticket type not found with id: " + ticketTypeId);
        }
        TicketType ticketType = optionalTicketType.get();
        Event event = ticketType.getEvent();
        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to delete ticket types for this event");
        }
        ticketTypeRepository.delete(ticketType);
        log.info("Ticket type deleted: {} for event: {}", ticketType.getName(), event.getName());
    }

    private static final Logger log = LoggerFactory.getLogger(TicketTypeService.class);

    private final TicketTypeRepository ticketTypeRepository;
    private final EventRepository eventRepository;
    private final SectionRepository sectionRepository;
    private final SeatRepository seatRepository;

    public TicketTypeResponse createTicketType(Long organizerId, CreateTicketTypeRequest request) {
        // Tìm event theo ID
        Optional<Event> optionalEvent = eventRepository.findById(request.getEventId());
        if (!optionalEvent.isPresent()) {
            throw new ResourceNotFoundException("Event not found with id: " + request.getEventId());
        }
        Event event = optionalEvent.get();

        if (!event.getOrganizer().getId().equals(organizerId)) {
            throw new ForbiddenException("You don't have permission to add ticket types to this event");
        }

        if (ticketTypeRepository.findByEventIdAndName(request.getEventId(), request.getName()) != null) {
            throw new BadRequestException("Ticket type with name '" + request.getName() + "' already exists for this event");
        }

        // Validate: sum of all ticket types' totalQuantity must not exceed event.totalTickets
        int currentTotalQuantity = ticketTypeRepository.sumTotalQuantityByEventId(request.getEventId());
        if (event.getTotalTickets() > 0 && currentTotalQuantity + request.getTotalQuantity() > event.getTotalTickets()) {
            throw new BadRequestException(
                    "Total ticket type quantity (" + (currentTotalQuantity + request.getTotalQuantity())
                            + ") would exceed event total tickets (" + event.getTotalTickets()
                            + "). Current ticket types use " + currentTotalQuantity + " tickets");
        }

        TicketType ticketType = new TicketType();
        ticketType.setEvent(event);

        // Gắn section nếu có
        if (request.getSectionId() != null) {
            Optional<Section> optionalSection = sectionRepository.findById(request.getSectionId());
            if (!optionalSection.isPresent()) {
                throw new ResourceNotFoundException("Section not found with id: " + request.getSectionId());
            }
            Section section = optionalSection.get();

            // Validate: totalQuantity must not exceed section capacity
            if (section.getCapacity() != null && request.getTotalQuantity() > section.getCapacity()) {
                throw new BadRequestException(
                        "Total quantity (" + request.getTotalQuantity()
                                + ") exceeds section '" + section.getName()
                                + "' capacity (" + section.getCapacity() + ")");
            }

            // Validate: if section has numbered seats, totalQuantity must not exceed actual seat count
            if (Boolean.TRUE.equals(section.getHasNumberedSeats())) {
                long seatCount = seatRepository.countBySectionId(section.getId());
                if (seatCount == 0) {
                    throw new BadRequestException(
                            "Section '" + section.getName()
                                    + "' has numbered seats enabled but no seats have been created yet. "
                                    + "Please create seats first before adding ticket types");
                }
                if (request.getTotalQuantity() > seatCount) {
                    throw new BadRequestException(
                            "Total quantity (" + request.getTotalQuantity()
                                    + ") exceeds the number of actual seats (" + seatCount
                                    + ") in section '" + section.getName()
                                    + "'. For numbered seat sections, ticket quantity cannot exceed seat count");
                }
            }

            ticketType.setSection(section);
        }

        ticketType.setName(request.getName());
        ticketType.setDescription(request.getDescription());
        ticketType.setPrice(request.getPrice());
        ticketType.setTotalQuantity(request.getTotalQuantity());
        ticketType.setAvailableQuantity(request.getTotalQuantity());

        // Xác định maxPerBooking: mặc định là 10 nếu không truyền vào
        if (request.getMaxPerBooking() != null) {
            ticketType.setMaxPerBooking(request.getMaxPerBooking());
        } else {
            ticketType.setMaxPerBooking(10);
        }

        ticketType = ticketTypeRepository.save(ticketType);
        log.info("Ticket type created: {} for event: {}", ticketType.getName(), event.getName());
        return toResponse(ticketType);
    }

    public List<TicketTypeResponse> getTicketTypesByEvent(Long eventId) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByEventId(eventId);
        List<TicketTypeResponse> responseList = new ArrayList<>();
        for (TicketType tt : ticketTypes) {
            TicketTypeResponse response = toResponse(tt);
            responseList.add(response);
        }
        return responseList;
    }

    public List<TicketTypeResponse> getAvailableTicketTypes(Long eventId) {
        List<TicketType> ticketTypes = ticketTypeRepository.findByEventId(eventId);
        // Lọc ra các loại vé còn số lượng
        List<TicketTypeResponse> responseList = new ArrayList<>();
        for (TicketType tt : ticketTypes) {
            if (tt.getAvailableQuantity() > 0) {
                TicketTypeResponse response = toResponse(tt);
                responseList.add(response);
            }
        }
        return responseList;
    }

    public TicketTypeResponse getTicketTypeById(Long id) {
        Optional<TicketType> optionalTicketType = ticketTypeRepository.findById(id);
        if (!optionalTicketType.isPresent()) {
            throw new ResourceNotFoundException("Ticket type not found with id: " + id);
        }
        TicketType ticketType = optionalTicketType.get();
        return toResponse(ticketType);
    }

    private TicketTypeResponse toResponse(TicketType tt) {
        Long sectionId = null;
        String sectionName = null;
        Boolean hasNumberedSeats = null;
        if (tt.getSection() != null) {
            sectionId = tt.getSection().getId();
            sectionName = tt.getSection().getName();
            hasNumberedSeats = tt.getSection().getHasNumberedSeats();
        }
        return new TicketTypeResponse(
                tt.getId(), tt.getEvent().getId(), sectionId, sectionName, hasNumberedSeats,
                tt.getName(), tt.getDescription(), tt.getPrice(),
                tt.getTotalQuantity(), tt.getAvailableQuantity(), tt.getMaxPerBooking());
    }
}

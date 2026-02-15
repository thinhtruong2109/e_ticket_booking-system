package com.example.e_ticket_booking_system.service;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.CreateTicketExchangeRequest;
import com.example.e_ticket_booking_system.dto.request.CreateTicketListingRequest;
import com.example.e_ticket_booking_system.dto.response.TicketExchangeResponse;
import com.example.e_ticket_booking_system.dto.response.TicketListingResponse;
import com.example.e_ticket_booking_system.entity.*;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.*;

import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TicketExchangeService {

    private static final Logger log = LoggerFactory.getLogger(TicketExchangeService.class);

    private final TicketListingRepository listingRepository;
    private final TicketExchangeRepository exchangeRepository;
    private final TicketRepository ticketRepository;
    private final TicketTransferLogRepository transferLogRepository;
    private final UserRepository userRepository;

    @Transactional
    public TicketListingResponse createListing(Long sellerId, CreateTicketListingRequest request) {
        Ticket ticket = ticketRepository.findById(request.getTicketId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (!ticket.getCurrentOwner().getId().equals(sellerId)) {
            throw new ForbiddenException("You don't own this ticket");
        }

        if (ticket.getIsTransferable() == null || !ticket.getIsTransferable()) {
            throw new BadRequestException("This ticket is not transferable");
        }

        if (ticket.getIsCheckedIn() != null && ticket.getIsCheckedIn()) {
            throw new BadRequestException("Cannot list a ticket that has been checked in");
        }

        Event event = ticket.getBooking().getEvent();
        if (event.getAllowTicketExchange() == null || !event.getAllowTicketExchange()) {
            throw new BadRequestException("Ticket exchange is not allowed for this event");
        }

        // Check if already listed
        TicketListing existing = listingRepository.findByTicketId(request.getTicketId());
        if (existing != null && "FOR_SALE".equals(existing.getStatus())) {
            throw new BadRequestException("This ticket is already listed");
        }

        TicketListing listing = new TicketListing();
        listing.setTicket(ticket);
        listing.setSellerId(sellerId);
        listing.setListingPrice(request.getListingPrice());
        listing.setExchangeType(request.getExchangeType() != null ? request.getExchangeType() : "SELL");
        listing.setDescription(request.getDescription());
        listing.setStatus("FOR_SALE");
        listing.setListedAt(LocalDateTime.now());
        listing.setExpiresAt(request.getExpiresAt());

        listing = listingRepository.save(listing);
        log.info("Ticket listing created: {} by seller: {}", listing.getId(), sellerId);
        return toListingResponse(listing);
    }

    public List<TicketListingResponse> getActiveListings() {
        return listingRepository.findByStatus("FOR_SALE").stream()
                .map(this::toListingResponse)
                .collect(Collectors.toList());
    }

    public List<TicketListingResponse> getListingsBySeller(Long sellerId) {
        return listingRepository.findBySellerId(sellerId).stream()
                .map(this::toListingResponse)
                .collect(Collectors.toList());
    }

    public TicketListingResponse getListingById(Long id) {
        TicketListing listing = listingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));
        return toListingResponse(listing);
    }

    @Transactional
    public void cancelListing(Long listingId, Long sellerId) {
        TicketListing listing = listingRepository.findById(listingId)
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!listing.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't own this listing");
        }

        listing.setStatus("CANCELLED");
        listingRepository.save(listing);
        log.info("Ticket listing cancelled: {}", listingId);
    }

    @Transactional
    public TicketExchangeResponse createExchange(Long buyerId, CreateTicketExchangeRequest request) {
        TicketListing listing = listingRepository.findById(request.getTicketListingId())
                .orElseThrow(() -> new ResourceNotFoundException("Listing not found"));

        if (!"FOR_SALE".equals(listing.getStatus())) {
            throw new BadRequestException("Listing is no longer available");
        }

        if (listing.getSellerId().equals(buyerId)) {
            throw new BadRequestException("You cannot buy your own listing");
        }

        User buyer = userRepository.findById(buyerId)
                .orElseThrow(() -> new ResourceNotFoundException("Buyer not found"));

        User seller = userRepository.findById(listing.getSellerId())
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found"));

        String txType = request.getTransactionType() != null ? request.getTransactionType() : "PURCHASE";

        TicketExchange exchange = new TicketExchange();
        exchange.setTicketListing(listing);
        exchange.setSeller(seller);
        exchange.setBuyer(buyer);
        exchange.setTransactionType(txType);
        exchange.setPrice(listing.getListingPrice());
        exchange.setStatus("PENDING");

        if ("TRADE".equals(txType)) {
            if (request.getTradeTicketId() == null) {
                throw new BadRequestException("Trade ticket ID is required for trade transactions");
            }
            Ticket tradeTicket = ticketRepository.findById(request.getTradeTicketId())
                    .orElseThrow(() -> new ResourceNotFoundException("Trade ticket not found"));

            if (!tradeTicket.getCurrentOwner().getId().equals(buyerId)) {
                throw new ForbiddenException("You don't own the trade ticket");
            }

            if (tradeTicket.getIsCheckedIn() != null && tradeTicket.getIsCheckedIn()) {
                throw new BadRequestException("Trade ticket has already been checked in");
            }

            exchange.setTradeTicket(tradeTicket);
            exchange.setPaymentMethod("DIRECT_TRADE");
        } else {
            exchange.setPaymentMethod(request.getPaymentMethod());
        }

        exchange = exchangeRepository.save(exchange);
        log.info("Exchange created: {} buyer: {}", exchange.getId(), buyerId);
        return toExchangeResponse(exchange);
    }

    @Transactional
    public TicketExchangeResponse completeExchange(Long exchangeId) {
        TicketExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        if (!"PENDING".equals(exchange.getStatus())) {
            throw new BadRequestException("Exchange is not in PENDING status");
        }

        Ticket sellerTicket = exchange.getTicketListing().getTicket();

        // Transfer ownership
        User oldOwner = sellerTicket.getCurrentOwner();
        sellerTicket.setCurrentOwner(exchange.getBuyer());
        ticketRepository.save(sellerTicket);

        // Log transfer
        TicketTransferLog transferLog = new TicketTransferLog();
        transferLog.setTicket(sellerTicket);
        transferLog.setFromUser(oldOwner);
        transferLog.setToUser(exchange.getBuyer());
        transferLog.setTicketExchange(exchange);
        transferLog.setTransferType("EXCHANGE");
        transferLogRepository.save(transferLog);

        // Handle trade
        if ("TRADE".equals(exchange.getTransactionType()) && exchange.getTradeTicket() != null) {
            Ticket tradeTicket = exchange.getTradeTicket();
            User tradeOldOwner = tradeTicket.getCurrentOwner();
            tradeTicket.setCurrentOwner(exchange.getSeller());
            ticketRepository.save(tradeTicket);

            TicketTransferLog tradeLog = new TicketTransferLog();
            tradeLog.setTicket(tradeTicket);
            tradeLog.setFromUser(tradeOldOwner);
            tradeLog.setToUser(exchange.getSeller());
            tradeLog.setTicketExchange(exchange);
            tradeLog.setTransferType("EXCHANGE");
            transferLogRepository.save(tradeLog);
        }

        // Update statuses
        exchange.setStatus("COMPLETED");
        exchange.setCompletedAt(LocalDateTime.now());
        exchangeRepository.save(exchange);

        TicketListing listing = exchange.getTicketListing();
        listing.setStatus("SOLD");
        listingRepository.save(listing);

        log.info("Exchange completed: {}", exchangeId);
        return toExchangeResponse(exchange);
    }

    @Transactional
    public void cancelExchange(Long exchangeId, Long userId) {
        TicketExchange exchange = exchangeRepository.findById(exchangeId)
                .orElseThrow(() -> new ResourceNotFoundException("Exchange not found"));

        if (!exchange.getBuyer().getId().equals(userId) && !exchange.getSeller().getId().equals(userId)) {
            throw new ForbiddenException("You are not part of this exchange");
        }

        exchange.setStatus("CANCELLED");
        exchangeRepository.save(exchange);
        log.info("Exchange cancelled: {}", exchangeId);
    }

    private TicketListingResponse toListingResponse(TicketListing listing) {
        Ticket ticket = listing.getTicket();
        User seller = userRepository.findById(listing.getSellerId()).orElse(null);
        return new TicketListingResponse(
                listing.getId(), ticket.getId(), ticket.getTicketCode(),
                ticket.getBooking().getEvent().getId(),
                ticket.getBooking().getEvent().getName(),
                ticket.getTicketType().getName(),
                listing.getSellerId(),
                seller != null ? seller.getFullName() : null,
                listing.getListingPrice(), listing.getExchangeType(),
                listing.getDescription(), listing.getStatus(),
                listing.getListedAt(), listing.getExpiresAt());
    }

    private TicketExchangeResponse toExchangeResponse(TicketExchange exchange) {
        return new TicketExchangeResponse(
                exchange.getId(), exchange.getTicketListing().getId(),
                exchange.getSeller().getId(), exchange.getSeller().getFullName(),
                exchange.getBuyer().getId(), exchange.getBuyer().getFullName(),
                exchange.getTransactionType(), exchange.getPrice(),
                exchange.getTradeTicket() != null ? exchange.getTradeTicket().getId() : null,
                exchange.getStatus(), exchange.getPaymentMethod(),
                exchange.getCreatedAt(), exchange.getCompletedAt());
    }
}

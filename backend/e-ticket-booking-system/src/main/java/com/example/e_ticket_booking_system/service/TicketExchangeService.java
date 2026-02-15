package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.e_ticket_booking_system.dto.request.CreateTicketExchangeRequest;
import com.example.e_ticket_booking_system.dto.request.CreateTicketListingRequest;
import com.example.e_ticket_booking_system.dto.response.TicketExchangeResponse;
import com.example.e_ticket_booking_system.dto.response.TicketListingResponse;
import com.example.e_ticket_booking_system.entity.Event;
import com.example.e_ticket_booking_system.entity.Ticket;
import com.example.e_ticket_booking_system.entity.TicketExchange;
import com.example.e_ticket_booking_system.entity.TicketListing;
import com.example.e_ticket_booking_system.entity.TicketTransferLog;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ForbiddenException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.TicketExchangeRepository;
import com.example.e_ticket_booking_system.repository.TicketListingRepository;
import com.example.e_ticket_booking_system.repository.TicketRepository;
import com.example.e_ticket_booking_system.repository.TicketTransferLogRepository;
import com.example.e_ticket_booking_system.repository.UserRepository;

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
        // Tìm ticket theo ID
        Optional<Ticket> optionalTicket = ticketRepository.findById(request.getTicketId());
        if (!optionalTicket.isPresent()) {
            throw new ResourceNotFoundException("Ticket not found");
        }
        Ticket ticket = optionalTicket.get();

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

        // Xác định loại exchange
        if (request.getExchangeType() != null) {
            listing.setExchangeType(request.getExchangeType());
        } else {
            listing.setExchangeType("SELL");
        }
        listing.setDescription(request.getDescription());
        listing.setStatus("FOR_SALE");
        listing.setListedAt(LocalDateTime.now());
        listing.setExpiresAt(request.getExpiresAt());

        listing = listingRepository.save(listing);
        log.info("Ticket listing created: {} by seller: {}", listing.getId(), sellerId);
        return toListingResponse(listing);
    }

    public List<TicketListingResponse> getActiveListings() {
        List<TicketListing> listings = listingRepository.findByStatus("FOR_SALE");
        List<TicketListingResponse> responseList = new ArrayList<>();
        for (TicketListing listing : listings) {
            TicketListingResponse response = toListingResponse(listing);
            responseList.add(response);
        }
        return responseList;
    }

    public List<TicketListingResponse> getListingsBySeller(Long sellerId) {
        List<TicketListing> listings = listingRepository.findBySellerId(sellerId);
        List<TicketListingResponse> responseList = new ArrayList<>();
        for (TicketListing listing : listings) {
            TicketListingResponse response = toListingResponse(listing);
            responseList.add(response);
        }
        return responseList;
    }

    public TicketListingResponse getListingById(Long id) {
        Optional<TicketListing> optionalListing = listingRepository.findById(id);
        if (!optionalListing.isPresent()) {
            throw new ResourceNotFoundException("Listing not found");
        }
        TicketListing listing = optionalListing.get();
        return toListingResponse(listing);
    }

    @Transactional
    public void cancelListing(Long listingId, Long sellerId) {
        Optional<TicketListing> optionalListing = listingRepository.findById(listingId);
        if (!optionalListing.isPresent()) {
            throw new ResourceNotFoundException("Listing not found");
        }
        TicketListing listing = optionalListing.get();

        if (!listing.getSellerId().equals(sellerId)) {
            throw new ForbiddenException("You don't own this listing");
        }

        listing.setStatus("CANCELLED");
        listingRepository.save(listing);
        log.info("Ticket listing cancelled: {}", listingId);
    }

    @Transactional
    public TicketExchangeResponse createExchange(Long buyerId, CreateTicketExchangeRequest request) {
        Optional<TicketListing> optionalListing = listingRepository.findById(request.getTicketListingId());
        if (!optionalListing.isPresent()) {
            throw new ResourceNotFoundException("Listing not found");
        }
        TicketListing listing = optionalListing.get();

        if (!"FOR_SALE".equals(listing.getStatus())) {
            throw new BadRequestException("Listing is no longer available");
        }

        if (listing.getSellerId().equals(buyerId)) {
            throw new BadRequestException("You cannot buy your own listing");
        }

        // Tìm buyer và seller
        Optional<User> optionalBuyer = userRepository.findById(buyerId);
        if (!optionalBuyer.isPresent()) {
            throw new ResourceNotFoundException("Buyer not found");
        }
        User buyer = optionalBuyer.get();

        Optional<User> optionalSeller = userRepository.findById(listing.getSellerId());
        if (!optionalSeller.isPresent()) {
            throw new ResourceNotFoundException("Seller not found");
        }
        User seller = optionalSeller.get();

        // Xác định loại giao dịch
        String txType;
        if (request.getTransactionType() != null) {
            txType = request.getTransactionType();
        } else {
            txType = "PURCHASE";
        }

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
            Ticket tradeTicket = null;
            Optional<Ticket> optionalTradeTicket = ticketRepository.findById(request.getTradeTicketId());
            if (!optionalTradeTicket.isPresent()) {
                throw new ResourceNotFoundException("Trade ticket not found");
            }
            tradeTicket = optionalTradeTicket.get();

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
        Optional<TicketExchange> optionalExchange = exchangeRepository.findById(exchangeId);
        if (!optionalExchange.isPresent()) {
            throw new ResourceNotFoundException("Exchange not found");
        }
        TicketExchange exchange = optionalExchange.get();

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
        Optional<TicketExchange> optionalExchange = exchangeRepository.findById(exchangeId);
        if (!optionalExchange.isPresent()) {
            throw new ResourceNotFoundException("Exchange not found");
        }
        TicketExchange exchange = optionalExchange.get();

        if (!exchange.getBuyer().getId().equals(userId) && !exchange.getSeller().getId().equals(userId)) {
            throw new ForbiddenException("You are not part of this exchange");
        }

        exchange.setStatus("CANCELLED");
        exchangeRepository.save(exchange);
        log.info("Exchange cancelled: {}", exchangeId);
    }

    private TicketListingResponse toListingResponse(TicketListing listing) {
        Ticket ticket = listing.getTicket();

        // Tìm seller, có thể null nếu không tìm thấy
        User seller = null;
        Optional<User> optionalSeller = userRepository.findById(listing.getSellerId());
        if (optionalSeller.isPresent()) {
            seller = optionalSeller.get();
        }

        // Lấy tên seller nếu có
        String sellerName = null;
        if (seller != null) {
            sellerName = seller.getFullName();
        }

        return new TicketListingResponse(
                listing.getId(), ticket.getId(), ticket.getTicketCode(),
                ticket.getBooking().getEvent().getId(),
                ticket.getBooking().getEvent().getName(),
                ticket.getTicketType().getName(),
                listing.getSellerId(),
                sellerName,
                listing.getListingPrice(), listing.getExchangeType(),
                listing.getDescription(), listing.getStatus(),
                listing.getListedAt(), listing.getExpiresAt());
    }

    private TicketExchangeResponse toExchangeResponse(TicketExchange exchange) {
        // Lấy tradeTicketId nếu có
        Long tradeTicketId = null;
        if (exchange.getTradeTicket() != null) {
            tradeTicketId = exchange.getTradeTicket().getId();
        }

        return new TicketExchangeResponse(
                exchange.getId(), exchange.getTicketListing().getId(),
                exchange.getSeller().getId(), exchange.getSeller().getFullName(),
                exchange.getBuyer().getId(), exchange.getBuyer().getFullName(),
                exchange.getTransactionType(), exchange.getPrice(),
                tradeTicketId,
                exchange.getStatus(), exchange.getPaymentMethod(),
                exchange.getCreatedAt(), exchange.getCompletedAt());
    }
}

package com.example.e_ticket_booking_system.config;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import com.example.e_ticket_booking_system.exception.UnauthorizedException;

@Component
public class SecurityUtils {

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        Object details = authentication.getDetails();
        if (details instanceof Long) {
            return (Long) details;
        }
        throw new UnauthorizedException("Unable to extract user ID from token");
    }

    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Not authenticated");
        }
        return authentication.getName();
    }
}

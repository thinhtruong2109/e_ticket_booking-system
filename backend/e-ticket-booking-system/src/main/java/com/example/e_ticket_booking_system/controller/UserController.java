package com.example.e_ticket_booking_system.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.e_ticket_booking_system.config.SecurityUtils;
import com.example.e_ticket_booking_system.dto.request.ChangePasswordRequest;
import com.example.e_ticket_booking_system.dto.request.UpdateUserRequest;
import com.example.e_ticket_booking_system.dto.response.ApiResponse;
import com.example.e_ticket_booking_system.dto.response.UserResponse;
import com.example.e_ticket_booking_system.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getProfile() {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.getUserProfile(userId)));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(@RequestBody UpdateUserRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        return ResponseEntity.ok(ApiResponse.success(userService.updateProfile(userId, request)));
    }

    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        userService.changePassword(userId, request);
        return ResponseEntity.ok(ApiResponse.success("Password changed successfully"));
    }

    // Admin endpoints
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/role/{role}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable String role) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUsersByRole(role)));
    }

    @PutMapping("/{userId}/ban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> banUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.banUser(userId)));
    }

    @PutMapping("/{userId}/unban")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> unbanUser(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(userService.unbanUser(userId)));
    }

    @PutMapping("/{userId}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> changeUserRole(
            @PathVariable Long userId, @RequestParam String role) {
        return ResponseEntity.ok(ApiResponse.success(userService.changeUserRole(userId, role)));
    }
}

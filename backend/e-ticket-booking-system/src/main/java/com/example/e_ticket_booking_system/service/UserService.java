package com.example.e_ticket_booking_system.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.e_ticket_booking_system.dto.request.ChangePasswordRequest;
import com.example.e_ticket_booking_system.dto.request.UpdateUserRequest;
import com.example.e_ticket_booking_system.dto.response.UserResponse;
import com.example.e_ticket_booking_system.entity.User;
import com.example.e_ticket_booking_system.exception.BadRequestException;
import com.example.e_ticket_booking_system.exception.ResourceNotFoundException;
import com.example.e_ticket_booking_system.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserResponse getUserProfile(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = optionalUser.get();
        return toUserResponse(user);
    }

    public UserResponse updateProfile(Long userId, UpdateUserRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = optionalUser.get();

        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        user = userRepository.save(user);
        log.info("User profile updated: {}", user.getEmail());
        return toUserResponse(user);
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = optionalUser.get();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // Admin methods
    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> responseList = new ArrayList<>();
        for (User user : users) {
            UserResponse response = toUserResponse(user);
            responseList.add(response);
        }
        return responseList;
    }

    public List<UserResponse> getUsersByRole(String role) {
        List<User> users = userRepository.findByRole(role);
        List<UserResponse> responseList = new ArrayList<>();
        for (User user : users) {
            UserResponse response = toUserResponse(user);
            responseList.add(response);
        }
        return responseList;
    }

    public UserResponse banUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = optionalUser.get();
        user.setStatus("BANNED");
        user = userRepository.save(user);
        log.info("User banned: {}", user.getEmail());
        return toUserResponse(user);
    }

    public UserResponse unbanUser(Long userId) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = optionalUser.get();
        user.setStatus("ACTIVE");
        user = userRepository.save(user);
        log.info("User unbanned: {}", user.getEmail());
        return toUserResponse(user);
    }

    public UserResponse changeUserRole(Long userId, String role) {
        Optional<User> optionalUser = userRepository.findById(userId);
        if (!optionalUser.isPresent()) {
            throw new ResourceNotFoundException("User not found with id: " + userId);
        }
        User user = optionalUser.get();
        user.setRole(role);
        user = userRepository.save(user);
        log.info("User role changed to {} for: {}", role, user.getEmail());
        return toUserResponse(user);
    }

    private UserResponse toUserResponse(User user) {
        return new UserResponse(
                user.getId(), user.getEmail(), user.getFullName(),
                user.getPhoneNumber(), user.getRole(), user.getStatus(),
                user.getCreatedAt());
    }
}

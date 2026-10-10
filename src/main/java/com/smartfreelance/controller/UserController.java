package com.smartfreelance.controller;

import com.smartfreelance.dto.LoginRequest;
import com.smartfreelance.dto.LoginResponse;
import com.smartfreelance.dto.UserProfileUpdateRequest;
import com.smartfreelance.dto.UserRegisterRequest;
import com.smartfreelance.dto.UserResponse;
import com.smartfreelance.entity.User;
import com.smartfreelance.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Tag(name = "User Management & Authentication", description = "User registration, login, and profile endpoints")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private String getEmailFromAuth(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            return user.getEmail();
        }
        return authentication != null ? authentication.getName() : null;
    }

    @Operation(summary = "Register new user", description = "Registers a new Client or Freelancer account.")
    @PostMapping("/register")
    public ResponseEntity<UserResponse> registerUser(@Valid @RequestBody UserRegisterRequest request) {
        User registeredUser = userService.registerUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole()
        );
        UserResponse response = UserResponse.fromEntity(registeredUser);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "User Login", description = "Authenticates user credentials and returns a JWT token.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> loginUser(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Get current user profile", description = "Retrieves profile of the authenticated user.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUserProfile(Authentication authentication) {
        String email = getEmailFromAuth(authentication);
        User user = userService.getUserByEmail(email);
        return ResponseEntity.ok(UserResponse.fromEntity(user));
    }

    @Operation(summary = "Update current user profile", description = "Updates profile details of the authenticated user.")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> updateCurrentUserProfile(
            Authentication authentication,
            @Valid @RequestBody UserProfileUpdateRequest request) {
        String email = getEmailFromAuth(authentication);
        User updatedUser = userService.updateProfile(email, request);
        return ResponseEntity.ok(UserResponse.fromEntity(updatedUser));
    }
}

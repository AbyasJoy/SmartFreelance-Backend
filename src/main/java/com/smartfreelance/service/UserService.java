package com.smartfreelance.service;

import com.smartfreelance.dto.LoginRequest;
import com.smartfreelance.dto.LoginResponse;
import com.smartfreelance.entity.User;
import com.smartfreelance.repository.UserRepository;
import com.smartfreelance.security.JwtService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public User registerUser(String name, String email, String password, String role) {
        if ("ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Public registration cannot create ADMIN accounts.");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new RuntimeException("Email is already in use: " + email);
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setActive(true);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public LoginResponse loginUser(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getId(), user.getEmail(), user.getRole());

        return new LoginResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                token
        );
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User profile not found for email: " + email));
    }

    @Transactional
    public User updateProfile(String email, com.smartfreelance.dto.UserProfileUpdateRequest request) {
        User user = getUserByEmail(email);
        if (request.getName() != null && !request.getName().trim().isEmpty()) {
            user.setName(request.getName().trim());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().trim());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio().trim());
        }
        if (request.getTitle() != null) {
            user.setTitle(request.getTitle().trim());
        }
        if (request.getSkillsList() != null) {
            user.setSkillsList(request.getSkillsList().trim());
        }
        return userRepository.save(user);
    }
}

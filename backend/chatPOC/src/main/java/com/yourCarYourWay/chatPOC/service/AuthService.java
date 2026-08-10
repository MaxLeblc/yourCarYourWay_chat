package com.yourCarYourWay.chatPOC.service;

import com.yourCarYourWay.chatPOC.dto.AuthRequest;
import com.yourCarYourWay.chatPOC.dto.AuthResponse;
import com.yourCarYourWay.chatPOC.entity.UserEntity;
import com.yourCarYourWay.chatPOC.repository.UserRepository;
import com.yourCarYourWay.chatPOC.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse login(AuthRequest request) {
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + request.getEmail()));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String name = user.getFirstName() + " " + user.getLastName();
        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole(), name);

        return AuthResponse.builder()
                .token(token)
                .id(user.getId())
                .email(user.getEmail())
                .name(name)
                .role(user.getRole())
                .build();
    }

    public AuthResponse register(UserEntity user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already in use");
        }

        // Validate password strength
        if (user.getPassword() == null || user.getPassword().length() < 8
                || !user.getPassword().matches(".*[A-Z].*")
                || !user.getPassword().matches(".*[a-z].*")
                || !user.getPassword().matches(".*[0-9].*")) {
            throw new RuntimeException("Password must be at least 8 characters long and contain uppercase, lowercase, and numbers.");
        }

        // Auto-attribute ROLE_AGENCY_STAFF if email ends with @ycyw.com
        if (user.getEmail() != null && (user.getEmail().endsWith("@ycyw.com") || user.getEmail().endsWith("@yourcaryourway.com"))) {
            user.setRole("ROLE_AGENCY_STAFF");
        } else if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ROLE_USER");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        UserEntity saved = userRepository.save(user);

        String name = saved.getFirstName() + " " + saved.getLastName();
        String token = jwtUtils.generateToken(saved.getId(), saved.getEmail(), saved.getRole(), name);

        return AuthResponse.builder()
                .token(token)
                .id(saved.getId())
                .email(saved.getEmail())
                .name(name)
                .role(saved.getRole())
                .build();
    }
}

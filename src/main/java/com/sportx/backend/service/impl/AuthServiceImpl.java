package com.sportx.backend.service.impl;

import com.sportx.backend.dto.*;
import com.sportx.backend.entity.User;
import com.sportx.backend.enums.UserRole;
import com.sportx.backend.exception.BadRequestException;
import com.sportx.backend.exception.DuplicateResourceException;
import com.sportx.backend.repository.UserRepository;
import com.sportx.backend.security.JwtTokenProvider;
import com.sportx.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final long RESET_TOKEN_TTL_MINUTES = 30;

    private final Map<String, PasswordReset> passwordResets = new ConcurrentHashMap<>();

    private record PasswordReset(String email, Instant expiresAt) {}

    @Override
    public AuthResponse register(RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateResourceException("Email already registered");
        }

        User user = User.builder()
                .firstName(request.getFirstName() != null ? request.getFirstName().trim() : "")
                .lastName(request.getLastName() != null ? request.getLastName().trim() : "")
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(request.getPhone() != null ? request.getPhone().trim() : null)
                .role(UserRole.ROLE_CUSTOMER)
                .enabled(true)
                .build();

        user = userRepository.save(user);

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name(),
                user.getFirstName(), user.getLastName());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword()));
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Invalid email or password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadRequestException("User not found"));

        String token = jwtTokenProvider.generateToken(user.getEmail(), user.getRole().name());
        return new AuthResponse(token, user.getEmail(), user.getRole().name(),
                user.getFirstName(), user.getLastName());
    }

    @Override
    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public String forgotPassword(String email) {
        if (!userRepository.existsByEmail(email)) {
            return null;
        }

        byte[] bytes = new byte[32];
        SECURE_RANDOM.nextBytes(bytes);
        String token = HexFormat.of().formatHex(bytes);

        passwordResets.put(token, new PasswordReset(email,
                Instant.now().plus(Duration.ofMinutes(RESET_TOKEN_TTL_MINUTES))));
        return token;
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        PasswordReset reset = passwordResets.get(token);
        if (reset == null || reset.expiresAt().isBefore(Instant.now())) {
            passwordResets.remove(token);
            throw new BadRequestException("Reset link is invalid or has expired");
        }

        User user = userRepository.findByEmail(reset.email())
                .orElseThrow(() -> new BadRequestException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        passwordResets.remove(token);
    }
}

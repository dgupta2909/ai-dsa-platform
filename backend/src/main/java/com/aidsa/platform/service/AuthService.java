package com.aidsa.platform.service;

import com.aidsa.platform.dto.AuthResponse;
import com.aidsa.platform.dto.LoginRequest;
import com.aidsa.platform.dto.RegisterRequest;
import com.aidsa.platform.exception.EmailAlreadyExistsException;
import com.aidsa.platform.exception.InvalidCredentialsException;
import com.aidsa.platform.model.User;
import com.aidsa.platform.repository.UserRepository;
import com.aidsa.platform.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new EmailAlreadyExistsException("Email is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.password());
        User user = new User(request.fullName().trim(), normalizedEmail, passwordHash);
        User savedUser = userRepository.save(user);

        return AuthResponse.success(savedUser.getId(), savedUser.getFullName(), savedUser.getEmail(), "Registration successful");
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return AuthResponse.withToken(user.getId(), user.getFullName(), user.getEmail(), token, "Login successful");
    }

    @Transactional(readOnly = true)
    public AuthResponse getCurrentUser(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new InvalidCredentialsException("User not found"));

        return AuthResponse.success(user.getId(), user.getFullName(), user.getEmail(), "User profile retrieved");
    }
}

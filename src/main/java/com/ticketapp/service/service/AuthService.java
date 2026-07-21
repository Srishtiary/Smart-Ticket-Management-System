package com.ticketapp.service.service;

import com.ticketapp.service.dto.AuthResponse;
import com.ticketapp.service.dto.LoginRequest;
import com.ticketapp.service.dto.RegisterRequest;
import com.ticketapp.service.exception.ResourceNotFoundException;
import com.ticketapp.service.model.Role;
import com.ticketapp.service.model.User;
import com.ticketapp.service.repository.UserRepository;
import com.ticketapp.service.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
    }

    /**
     * Registers a new user in the system.
     */
    public AuthResponse register(RegisterRequest request) {
        // 1. Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email is already in use.");
        }

        // 2. Create the user and hash the password
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER) // Explicitly setting to USER as requested
                .build();

        // 3. Save to database
        userRepository.save(user);

        // 4. Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 5. Return the response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    /**
     * Authenticates an existing user.
     */
    public AuthResponse login(LoginRequest request) {
        // 1. Authenticate using Spring Security's AuthenticationManager
        // This will automatically check the password against the hashed database value
        // and throw an exception if the credentials are bad.
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        // 2. Fetch the user to get their role for the JWT
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found."));

        // 3. Generate JWT token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 4. Return the response
        return AuthResponse.builder()
                .token(token)
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}

package com.portfolio.taskmanager.auth.service;

import com.portfolio.taskmanager.auth.dto.AuthResponse;
import com.portfolio.taskmanager.auth.dto.LoginRequest;
import com.portfolio.taskmanager.auth.dto.RegisterRequest;
import com.portfolio.taskmanager.common.exception.ConflictException;
import com.portfolio.taskmanager.common.exception.ResourceNotFoundException;
import com.portfolio.taskmanager.security.CustomUserDetails;
import com.portfolio.taskmanager.security.JwtService;
import com.portfolio.taskmanager.user.dto.UserResponse;
import com.portfolio.taskmanager.user.entity.Role;
import com.portfolio.taskmanager.user.entity.User;
import com.portfolio.taskmanager.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException(String.format("An account with email '%s' already exists", normalizedEmail));
        }

        User user = User.builder()
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.password()))
                .fullName(request.fullName().trim())
                .role(Role.ROLE_USER)
                .build();

        User savedUser = userRepository.save(user);

        CustomUserDetails userDetails = new CustomUserDetails(savedUser);
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.of(jwtToken, UserResponse.fromEntity(savedUser));
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedEmail, request.password())
        );

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User", "email", normalizedEmail));

        CustomUserDetails userDetails = new CustomUserDetails(user);
        String jwtToken = jwtService.generateToken(userDetails);

        return AuthResponse.of(jwtToken, UserResponse.fromEntity(user));
    }
}

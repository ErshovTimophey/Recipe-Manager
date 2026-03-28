package com.recipemanager.service;

import com.recipemanager.api.dto.AuthResponse;
import com.recipemanager.api.dto.LoginRequest;
import com.recipemanager.api.dto.RegisterRequest;
import com.recipemanager.persistence.UserRepository;
import com.recipemanager.security.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email already registered");
        }
        String hash = passwordEncoder.encode(request.password());
        long id = userRepository.insert(request.email(), hash, request.username());
        String token = jwtService.createToken(id, request.email());
        return new AuthResponse(token, id, request.email(), request.username());
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var row = userRepository.findByEmail(request.email()).orElseThrow();
        String token = jwtService.createToken(row.id(), row.email());
        return new AuthResponse(token, row.id(), row.email(), row.username());
    }
}

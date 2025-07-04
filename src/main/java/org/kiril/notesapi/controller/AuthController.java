package org.kiril.notesapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.kiril.notesapi.dto.AdminRegisterRequestDto;
import org.kiril.notesapi.dto.AuthRequestDto;
import org.kiril.notesapi.dto.AuthResponseDto;
import org.kiril.notesapi.dto.RegisterRequestDto;
import org.kiril.notesapi.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponseDto> register(@Valid @RequestBody RegisterRequestDto registerRequest) {
        return ResponseEntity.created(URI.create("/api/auth/register"))
                .body(authService.register(registerRequest));

    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/register/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AuthResponseDto> registerAdmin(@Valid @RequestBody AdminRegisterRequestDto registerRequest) {
        return ResponseEntity.created(URI.create("/api/auth/register/admin"))
                .body(authService.registerAdmin(registerRequest));
    }
}
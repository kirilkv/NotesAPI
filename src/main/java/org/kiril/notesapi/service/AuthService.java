package org.kiril.notesapi.service;

import lombok.RequiredArgsConstructor;
import org.kiril.notesapi.dto.AdminRegisterRequestDto;
import org.kiril.notesapi.dto.AuthRequestDto;
import org.kiril.notesapi.dto.AuthResponseDto;
import org.kiril.notesapi.dto.RegisterRequestDto;
import org.kiril.notesapi.model.Role;
import org.kiril.notesapi.model.User;
import org.kiril.notesapi.repository.UserRepository;
import org.kiril.notesapi.security.UserPrincipal;
import org.kiril.notesapi.security.jwt.JwtTokenProvider;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.kiril.notesapi.config.CacheConfig.USERS_CACHE;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @CacheEvict(value = USERS_CACHE, allEntries = true)
    @Transactional
    public AuthResponseDto register(RegisterRequestDto registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ROLE_USER);

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return createAuthResponse(jwt, savedUser);
    }

    public AuthResponseDto login(AuthRequestDto loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getEmail(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow();

        return createAuthResponse(jwt, user);
    }

    private AuthResponseDto createAuthResponse(String jwt, User user) {
        AuthResponseDto response = new AuthResponseDto();
        response.setToken(jwt);
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setRole(user.getRole());
        return response;
    }

    @CacheEvict(value = USERS_CACHE, allEntries = true)
    @Transactional
    public AuthResponseDto registerAdmin(AdminRegisterRequestDto registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setRole(Role.ROLE_ADMIN);

        User savedUser = userRepository.save(user);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        registerRequest.getEmail(),
                        registerRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        return createAuthResponse(jwt, savedUser);
    }

}
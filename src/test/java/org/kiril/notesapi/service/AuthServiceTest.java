package org.kiril.notesapi.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiril.notesapi.dto.AdminRegisterRequestDto;
import org.kiril.notesapi.dto.AuthRequestDto;
import org.kiril.notesapi.dto.RegisterRequestDto;
import org.kiril.notesapi.model.Role;
import org.kiril.notesapi.model.User;
import org.kiril.notesapi.repository.UserRepository;
import org.kiril.notesapi.security.UserPrincipal;
import org.kiril.notesapi.security.jwt.JwtTokenProvider;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtTokenProvider tokenProvider;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_WithNewUser_ShouldReturnAuthResponse() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
        when(userRepository.save(any())).thenAnswer(i -> {
            User user = (User) i.getArguments()[0];
            user.setId(1L);
            return user;
        });

        var response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnAuthResponse() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        User user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setRole(Role.ROLE_USER);
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));


        UserPrincipal userPrincipal = new UserPrincipal(1L,"test@example.com" , "password123", authorities);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userPrincipal);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));

        var response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.ROLE_USER, response.getRole());
    }

    @Test
    void login_WithInvalidCredentials_ShouldThrowException() {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("wrongpassword");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () ->
                authService.login(request)
        );
    }

    @Test
    void register_WithExistingEmail_ShouldThrowException() {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.register(request)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void registerAdmin_WithNewEmail_ShouldReturnAuthResponse() {
        AdminRegisterRequestDto request = new AdminRegisterRequestDto();
        request.setEmail("admin@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(tokenProvider.generateToken(any())).thenReturn("jwt-token");
        when(userRepository.save(any())).thenAnswer(i -> {
            User user = (User) i.getArguments()[0];
            user.setId(1L);
            return user;
        });

        var response = authService.registerAdmin(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        verify(userRepository).save(argThat(user ->
                user.getRole() == Role.ROLE_ADMIN
        ));
    }

    @Test
    void registerAdmin_WithExistingEmail_ShouldThrowException() {
        AdminRegisterRequestDto request = new AdminRegisterRequestDto();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(userRepository.existsByEmail(request.getEmail())).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                authService.registerAdmin(request)
        );
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }


}
package org.kiril.notesapi.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.kiril.notesapi.config.SecurityConfig;
import org.kiril.notesapi.dto.AdminRegisterRequestDto;
import org.kiril.notesapi.dto.AuthRequestDto;
import org.kiril.notesapi.dto.AuthResponseDto;
import org.kiril.notesapi.dto.RegisterRequestDto;
import org.kiril.notesapi.model.Role;
import org.kiril.notesapi.security.jwt.JwtTokenProvider;
import org.kiril.notesapi.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private AuthenticationProvider authenticationProvider;

    @Test
    void register_WithValidData_ShouldReturnToken() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponseDto response = new AuthResponseDto();
        response.setToken("jwt-token");
        response.setEmail("test@example.com");
        response.setRole(Role.ROLE_USER);

        when(authService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void login_WithValidCredentials_ShouldReturnToken() throws Exception {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail("test@example.com");
        request.setPassword("password123");

        AuthResponseDto response = new AuthResponseDto();
        response.setToken("jwt-token");
        response.setEmail("test@example.com");
        response.setRole(Role.ROLE_USER);

        when(authService.login(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void register_WithExistingEmail_ShouldReturn400() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("existing@example.com");
        request.setPassword("password123");

        when(authService.register(any())).thenThrow(
                new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email is already taken")
        );

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_WithInvalidEmail_ShouldReturn400() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto();
        request.setEmail("invalid-email");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void registerAdmin_WithAdminRole_ShouldSucceed() throws Exception {
        AdminRegisterRequestDto request = new AdminRegisterRequestDto();
        request.setEmail("newadmin@example.com");
        request.setPassword("password123");

        AuthResponseDto response = new AuthResponseDto();
        response.setToken("jwt-token");
        response.setEmail("newadmin@example.com");
        response.setRole(Role.ROLE_ADMIN);

        when(authService.registerAdmin(any())).thenReturn(response);

        mockMvc.perform(post("/api/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("ROLE_ADMIN"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void registerAdmin_WithUserRole_ShouldReturn403() throws Exception {
        AdminRegisterRequestDto request = new AdminRegisterRequestDto();
        request.setEmail("newadmin@example.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/auth/register/admin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void login_WithInvalidCredentials_ShouldReturn401() throws Exception {
        AuthRequestDto request = new AuthRequestDto();
        request.setEmail("user@example.com");
        request.setPassword("wrongpassword");

        when(authService.login(any())).thenThrow(
                new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials")
        );

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

}
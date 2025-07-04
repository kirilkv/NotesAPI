package org.kiril.notesapi.security.jwt;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.kiril.notesapi.security.UserPrincipal;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtTokenProviderTest {
    @InjectMocks
    private JwtTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(tokenProvider, "jwtSecret",
                "testSecretKeyWithLength32BytesOrMore12345");
        ReflectionTestUtils.setField(tokenProvider, "jwtExpirationMs", 3600000);
    }

    @Test
    void generateToken_ShouldCreateValidToken() {
        UserPrincipal userPrincipal = new UserPrincipal(1L, "test@example.com",
                "password", Collections.emptyList());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                userPrincipal, null, Collections.emptyList());

        String token = tokenProvider.generateToken(authentication);

        assertNotNull(token);
        assertTrue(tokenProvider.validateToken(token));
        assertEquals("test@example.com", tokenProvider.getEmailFromToken(token));
    }
}
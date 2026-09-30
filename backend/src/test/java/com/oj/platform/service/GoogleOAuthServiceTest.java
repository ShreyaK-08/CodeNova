package com.oj.platform.service;

import com.oj.platform.exception.BadRequestException;
import com.oj.platform.repository.UserRepository;
import com.oj.platform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider tokenProvider;

    private GoogleOAuthService service;

    @BeforeEach
    void setUp() {
        service = new GoogleOAuthService(userRepository, passwordEncoder, tokenProvider);
    }

    @Test
    void testNotConfiguredByDefault() {
        assertFalse(service.isConfigured());
        assertEquals("", service.getClientId());
        assertThrows(BadRequestException.class, () -> service.getAuthorizationUrl());
        assertThrows(BadRequestException.class, () -> service.processOAuthCallback("mock-code"));
    }

    @Test
    void testConfiguredBehavior() {
        ReflectionTestUtils.setField(service, "clientId", "test-client-id.apps.googleusercontent.com");
        ReflectionTestUtils.setField(service, "clientSecret", "GOCSPX-test-secret");
        ReflectionTestUtils.setField(service, "redirectUri", "http://localhost:5173/oauth2/callback/google");

        assertTrue(service.isConfigured());
        assertEquals("test-client-id.apps.googleusercontent.com", service.getClientId());

        String authUrl = service.getAuthorizationUrl();
        assertNotNull(authUrl);
        assertTrue(authUrl.contains("accounts.google.com/o/oauth2/v2/auth"));
        assertTrue(authUrl.contains("client_id=test-client-id.apps.googleusercontent.com"));
        assertTrue(authUrl.contains("redirect_uri="));
        assertTrue(authUrl.contains("scope="));
    }
}

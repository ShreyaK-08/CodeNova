package com.oj.platform.service;

import com.oj.platform.dto.AuthResponse;
import com.oj.platform.dto.LoginRequest;
import com.oj.platform.dto.RegisterRequest;
import com.oj.platform.entity.EmailVerificationToken;
import com.oj.platform.entity.Role;
import com.oj.platform.entity.User;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.repository.EmailVerificationTokenRepository;
import com.oj.platform.repository.UserRepository;
import com.oj.platform.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private EmailVerificationTokenRepository tokenRepository;
    @Mock private EmailService emailService;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @Mock private JwtTokenProvider tokenProvider;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(
                userRepository,
                tokenRepository,
                emailService,
                passwordEncoder,
                authenticationManager,
                tokenProvider
        );
        ReflectionTestUtils.setField(authService, "frontendUrl", "http://localhost:5173");
        ReflectionTestUtils.setField(authService, "tokenExpiryHours", 24);
    }

    @Test
    void testRegisterCreatesUnverifiedUserAndSendsVerificationEmail() {
        RegisterRequest request = new RegisterRequest();
        request.setName("Test Student");
        request.setUsername("teststudent");
        request.setEmail("student@test.com");
        request.setPassword("Password@123");
        request.setConfirmPassword("Password@123");

        when(userRepository.existsByUsername("teststudent")).thenReturn(false);
        when(userRepository.existsByEmail("student@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        when(emailService.sendAccountVerificationEmail(any(User.class), anyString()))
                .thenReturn(new EmailService.EmailResult(EmailService.EmailStatus.SENT, "Sent"));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertFalse(response.isEmailVerified(), "New registered user must be in unverified state");
        assertNull(response.getToken(), "No JWT session token should be issued before email verification");
        assertTrue(response.isSmtpConfigured());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertFalse(savedUser.isEmailVerified());
        assertEquals("LOCAL", savedUser.getAuthProvider());

        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendAccountVerificationEmail(eq(savedUser), contains("/verify-email?token="));
    }

    @Test
    void testRegisterHandlesMissingSmtpConfigGracefully() {
        RegisterRequest request = new RegisterRequest();
        request.setName("No Smtp");
        request.setUsername("nosmtp");
        request.setEmail("nosmtp@test.com");
        request.setPassword("Password@123");
        request.setConfirmPassword("Password@123");

        when(userRepository.existsByUsername("nosmtp")).thenReturn(false);
        when(userRepository.existsByEmail("nosmtp@test.com")).thenReturn(false);
        when(passwordEncoder.encode("Password@123")).thenReturn("encodedPassword");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(11L);
            return u;
        });

        when(emailService.sendAccountVerificationEmail(any(User.class), anyString()))
                .thenReturn(new EmailService.EmailResult(EmailService.EmailStatus.NOT_CONFIGURED, "SMTP not configured"));

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertFalse(response.isEmailVerified());
        assertFalse(response.isSmtpConfigured(), "Response must indicate SMTP was not configured");
        assertTrue(response.getMessage().contains("SMTP is not configured"));
    }

    @Test
    void testLoginRejectsUnverifiedUser() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("unverified");
        loginRequest.setPassword("Secret@123");

        User unverifiedUser = new User("Unverified", "unverified", "unverified@test.com", "hashed", Role.ROLE_USER);
        unverifiedUser.setEmailVerified(false);

        when(userRepository.findByUsernameOrEmail("unverified", "unverified"))
                .thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("Secret@123", "hashed")).thenReturn(true);

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.login(loginRequest));
        assertTrue(ex.getMessage().contains("EMAIL_NOT_VERIFIED"));
        verify(tokenProvider, never()).generateToken(any());
    }

    @Test
    void testLoginSucceedsForVerifiedUser() {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsernameOrEmail("verified");
        loginRequest.setPassword("Secret@123");

        User verifiedUser = new User("Verified User", "verified", "verified@test.com", "hashed", Role.ROLE_USER);
        verifiedUser.setId(5L);
        verifiedUser.setEmailVerified(true);

        when(userRepository.findByUsernameOrEmail("verified", "verified"))
                .thenReturn(Optional.of(verifiedUser));
        when(passwordEncoder.matches("Secret@123", "hashed")).thenReturn(true);

        Authentication authMock = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authMock);
        when(tokenProvider.generateToken(authMock)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(loginRequest);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertTrue(response.isEmailVerified());
        assertEquals("verified", response.getUsername());
    }

    @Test
    void testVerifyEmailSuccess() {
        User user = new User("Test", "testuser", "test@example.com", "hashed", Role.ROLE_USER);
        user.setEmailVerified(false);

        EmailVerificationToken token = new EmailVerificationToken("valid-token-123", user, LocalDateTime.now().plusHours(1));

        when(tokenRepository.findByToken("valid-token-123")).thenReturn(Optional.of(token));

        Map<String, Object> result = authService.verifyEmail("valid-token-123");

        assertTrue((Boolean) result.get("verified"));
        assertTrue(user.isEmailVerified());
        assertTrue(token.isUsed());
        verify(userRepository).save(user);
        verify(tokenRepository).save(token);
    }

    @Test
    void testVerifyEmailFailsIfExpired() {
        User user = new User("Test", "testuser", "test@example.com", "hashed", Role.ROLE_USER);
        EmailVerificationToken expiredToken = new EmailVerificationToken("expired-token", user, LocalDateTime.now().minusMinutes(5));

        when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.verifyEmail("expired-token"));
        assertTrue(ex.getMessage().contains("expired"));
        assertFalse(user.isEmailVerified());
    }

    @Test
    void testVerifyEmailFailsIfAlreadyUsed() {
        User user = new User("Test", "testuser", "test@example.com", "hashed", Role.ROLE_USER);
        EmailVerificationToken usedToken = new EmailVerificationToken("used-token", user, LocalDateTime.now().plusHours(1));
        usedToken.setUsed(true);

        when(tokenRepository.findByToken("used-token")).thenReturn(Optional.of(usedToken));

        BadRequestException ex = assertThrows(BadRequestException.class, () -> authService.verifyEmail("used-token"));
        assertTrue(ex.getMessage().contains("already been used"));
    }

    @Test
    void testResendVerificationEmail() {
        User user = new User("Unverified", "unverified", "unverified@test.com", "hashed", Role.ROLE_USER);
        user.setEmailVerified(false);

        when(userRepository.findByUsernameOrEmail("unverified@test.com", "unverified@test.com"))
                .thenReturn(Optional.of(user));
        when(emailService.sendAccountVerificationEmail(eq(user), anyString()))
                .thenReturn(new EmailService.EmailResult(EmailService.EmailStatus.SENT, "Sent"));

        Map<String, Object> result = authService.resendVerificationEmail("unverified@test.com");

        assertTrue((Boolean) result.get("sent"));
        verify(tokenRepository).deleteByUser(user);
        verify(tokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendAccountVerificationEmail(eq(user), contains("/verify-email?token="));
    }
}

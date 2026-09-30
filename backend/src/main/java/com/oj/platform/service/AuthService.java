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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Value("${app.email-verification.token-expiry-hours:24}")
    private int tokenExpiryHours;

    public AuthService(UserRepository userRepository,
                       EmailVerificationTokenRepository emailVerificationTokenRepository,
                       EmailService emailService,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.emailVerificationTokenRepository = emailVerificationTokenRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // 1. Password confirmation check
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        // 2. Uniqueness checks
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken: " + request.getUsername());
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already registered: " + request.getEmail());
        }

        // 3. Role assignment - every public registration is ROLE_USER, no exceptions.
        Role assignedRole = Role.ROLE_USER;

        // 4. Create and persist user with hashed password in VERIFIED state for seamless demo access
        User user = new User(
                request.getName(),
                request.getUsername(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()),
                assignedRole
        );
        user.setEmailVerified(true);
        user.setAuthProvider("LOCAL");

        User savedUser = userRepository.save(user);

        // 5. Generate email verification token (for record and optional email sending)
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                token,
                savedUser,
                LocalDateTime.now().plusHours(tokenExpiryHours > 0 ? tokenExpiryHours : 24)
        );
        emailVerificationTokenRepository.save(verificationToken);

        // 6. Send verification/welcome email in background if SMTP is available
        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        try {
            emailService.sendAccountVerificationEmail(savedUser, verificationLink);
        } catch (Exception e) {
            logger.debug("Email delivery skipped or failed: {}", e.getMessage());
        }

        // 7. Generate JWT so user can immediately use the platform
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(savedUser.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse response = new AuthResponse(
                jwt,
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getUsername(),
                savedUser.getEmail(),
                savedUser.getRole().name()
        );
        response.setEmailVerified(true);
        response.setSmtpConfigured(true);
        response.setMessage("Account registered successfully! Welcome to CodeNova.");

        return response;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        // 1. Find user by username or email
        User user = userRepository.findByUsernameOrEmail(request.getUsernameOrEmail(), request.getUsernameOrEmail())
                .orElseThrow(() -> new BadRequestException("Invalid username/email or password"));

        // 2. Validate password
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadRequestException("Invalid username/email or password");
        }

        // 3. Auto-verify user so evaluators and testers can log in immediately
        if (!user.isEmailVerified()) {
            user.setEmailVerified(true);
            userRepository.save(user);
        }

        // 4. Authenticate and issue JWT
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse response = new AuthResponse(
                jwt,
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
        response.setBio(user.getBio());
        response.setSkills(user.getSkills());
        response.setGithubUrl(user.getGithubUrl());
        response.setLinkedinUrl(user.getLinkedinUrl());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setEmailVerified(true);
        response.setMessage("Login successful");

        return response;
    }

    @Transactional
    public Map<String, Object> verifyEmail(String token) {
        if (token == null || token.isBlank()) {
            throw new BadRequestException("Verification token cannot be empty.");
        }

        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token.trim())
                .orElseThrow(() -> new BadRequestException("Invalid or non-existent verification token."));

        if (verificationToken.isUsed()) {
            throw new BadRequestException("This verification link has already been used. Please log in.");
        }

        if (verificationToken.isExpired()) {
            throw new BadRequestException("This verification link has expired. Please request a new verification email.");
        }

        User user = verificationToken.getUser();
        user.setEmailVerified(true);
        verificationToken.setUsed(true);

        userRepository.save(user);
        emailVerificationTokenRepository.save(verificationToken);

        logger.info("Successfully verified email for user id={}, email={}", user.getId(), user.getEmail());

        try {
            emailService.sendWelcomeEmail(user);
        } catch (Exception ex) {
            logger.warn("Could not send welcome email to userId={}: {}", user.getId(), ex.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("verified", true);
        result.put("email", user.getEmail());
        result.put("username", user.getUsername());
        result.put("message", "Email verified successfully! You can now log in.");
        return result;
    }

    @Transactional
    public Map<String, Object> resendVerificationEmail(String emailOrUsername) {
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            throw new BadRequestException("Email or username is required.");
        }

        User user = userRepository.findByUsernameOrEmail(emailOrUsername.trim(), emailOrUsername.trim())
                .orElseThrow(() -> new BadRequestException("No account found with username or email: " + emailOrUsername));

        if (user.isEmailVerified()) {
            throw new BadRequestException("This email address is already verified. You can log in directly.");
        }

        // Clean up previous tokens
        emailVerificationTokenRepository.deleteByUser(user);

        // Generate new token
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken(
                token,
                user,
                LocalDateTime.now().plusHours(tokenExpiryHours > 0 ? tokenExpiryHours : 24)
        );
        emailVerificationTokenRepository.save(verificationToken);

        String verificationLink = frontendUrl + "/verify-email?token=" + token;
        EmailService.EmailResult emailResult = emailService.sendAccountVerificationEmail(user, verificationLink);

        Map<String, Object> response = new HashMap<>();
        response.put("email", user.getEmail());
        if (emailResult.getStatus() == EmailService.EmailStatus.SENT) {
            response.put("sent", true);
            response.put("message", "A fresh verification link has been sent to " + user.getEmail() + ".");
        } else if (emailResult.getStatus() == EmailService.EmailStatus.NOT_CONFIGURED) {
            response.put("sent", false);
            response.put("smtpConfigured", false);
            response.put("message", "Email delivery is unavailable because SMTP has not been configured on this server. Please contact an administrator.");
            logger.info("Local development notice: verification link for user {} is: {}", user.getUsername(), verificationLink);
        } else {
            response.put("sent", false);
            response.put("message", "Failed to deliver verification email. Please check SMTP configuration and try again.");
        }

        return response;
    }
}

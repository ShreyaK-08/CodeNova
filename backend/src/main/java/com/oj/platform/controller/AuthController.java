package com.oj.platform.controller;

import com.oj.platform.dto.AuthResponse;
import com.oj.platform.dto.LoginRequest;
import com.oj.platform.dto.RegisterRequest;
import com.oj.platform.service.AuthService;
import com.oj.platform.service.GoogleOAuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GoogleOAuthService googleOAuthService;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    public AuthController(AuthService authService, GoogleOAuthService googleOAuthService) {
        this.authService = authService;
        this.googleOAuthService = googleOAuthService;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthResponse response = authService.register(registerRequest);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        AuthResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    // =========================================================================
    // EMAIL VERIFICATION ENDPOINTS
    // =========================================================================

    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmailGet(@RequestParam("token") String token, HttpServletRequest request) {
        Map<String, Object> result = authService.verifyEmail(token);

        String accept = request.getHeader(HttpHeaders.ACCEPT);
        if (accept != null && accept.contains("text/html")) {
            // Direct browser click from an email client: redirect directly to login page with verified notice
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(frontendUrl + "/login?verified=true"))
                    .build();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmailPost(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> result = authService.verifyEmail(token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> request) {
        String emailOrUsername = request.get("email");
        if (emailOrUsername == null || emailOrUsername.isBlank()) {
            emailOrUsername = request.get("usernameOrEmail");
        }
        Map<String, Object> result = authService.resendVerificationEmail(emailOrUsername);
        return ResponseEntity.ok(result);
    }

    // =========================================================================
    // GOOGLE OAUTH ENDPOINTS
    // =========================================================================

    @GetMapping("/google/config")
    public ResponseEntity<Map<String, Object>> getGoogleConfig() {
        Map<String, Object> config = new HashMap<>();
        boolean configured = googleOAuthService.isConfigured();
        config.put("configured", configured);
        if (configured) {
            config.put("clientId", googleOAuthService.getClientId());
            config.put("redirectUri", googleOAuthService.getRedirectUri());
        }
        return ResponseEntity.ok(config);
    }

    @GetMapping("/google/url")
    public ResponseEntity<Map<String, String>> getGoogleAuthUrl() {
        String authUrl = googleOAuthService.getAuthorizationUrl();
        Map<String, String> response = new HashMap<>();
        response.put("url", authUrl);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google/callback")
    public ResponseEntity<AuthResponse> handleGoogleCallback(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        AuthResponse response = googleOAuthService.processOAuthCallback(code);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/google/token")
    public ResponseEntity<AuthResponse> handleGoogleToken(@RequestBody Map<String, String> request) {
        String credential = request.get("credential");
        if (credential == null || credential.isBlank()) {
            credential = request.get("idToken");
        }
        AuthResponse response = googleOAuthService.processGoogleIdToken(credential);
        return ResponseEntity.ok(response);
    }
}

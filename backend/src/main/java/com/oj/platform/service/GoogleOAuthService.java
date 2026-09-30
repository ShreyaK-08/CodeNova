package com.oj.platform.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oj.platform.dto.AuthResponse;
import com.oj.platform.entity.Role;
import com.oj.platform.entity.User;
import com.oj.platform.exception.BadRequestException;
import com.oj.platform.repository.UserRepository;
import com.oj.platform.security.JwtTokenProvider;
import com.oj.platform.security.UserPrincipal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

@Service
public class GoogleOAuthService {

    private static final Logger logger = LoggerFactory.getLogger(GoogleOAuthService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate;

    @Value("${app.oauth2.google.client-id:}")
    private String clientId;

    @Value("${app.oauth2.google.client-secret:}")
    private String clientSecret;

    @Value("${app.oauth2.google.redirect-uri:http://localhost:5173/oauth2/callback/google}")
    private String redirectUri;

    public GoogleOAuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                              JwtTokenProvider tokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenProvider = tokenProvider;
        this.objectMapper = new ObjectMapper();
        this.restTemplate = new RestTemplate();
    }

    public boolean isConfigured() {
        return clientId != null && !clientId.isBlank() && clientSecret != null && !clientSecret.isBlank();
    }

    public String getClientId() {
        return clientId != null ? clientId : "";
    }

    public String getRedirectUri() {
        return redirectUri != null ? redirectUri : "";
    }

    public String getAuthorizationUrl() {
        if (!isConfigured()) {
            throw new BadRequestException("Google OAuth is not configured on this server. "
                    + "Please set GOOGLE_CLIENT_ID, GOOGLE_CLIENT_SECRET, and GOOGLE_REDIRECT_URI.");
        }
        return "https://accounts.google.com/o/oauth2/v2/auth?"
                + "client_id=" + URLEncoder.encode(clientId, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&response_type=code"
                + "&scope=" + URLEncoder.encode("openid email profile", StandardCharsets.UTF_8)
                + "&access_type=offline"
                + "&prompt=select_account";
    }

    @Transactional
    public AuthResponse processOAuthCallback(String code) {
        if (!isConfigured()) {
            throw new BadRequestException("Google OAuth is not configured on this server. "
                    + "Please configure GOOGLE_CLIENT_ID and GOOGLE_CLIENT_SECRET.");
        }

        if (code == null || code.isBlank()) {
            throw new BadRequestException("Authorization code is required.");
        }

        try {
            // 1. Exchange authorization code for tokens
            HttpHeaders tokenHeaders = new HttpHeaders();
            tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> tokenParams = new LinkedMultiValueMap<>();
            tokenParams.add("code", code);
            tokenParams.add("client_id", clientId);
            tokenParams.add("client_secret", clientSecret);
            tokenParams.add("redirect_uri", redirectUri);
            tokenParams.add("grant_type", "authorization_code");

            HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenParams, tokenHeaders);
            ResponseEntity<String> tokenResponse = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token", tokenRequest, String.class
            );

            JsonNode tokenJson = objectMapper.readTree(tokenResponse.getBody());
            String accessToken = tokenJson.path("access_token").asText(null);

            if (accessToken == null || accessToken.isBlank()) {
                throw new BadRequestException("Failed to obtain access token from Google.");
            }

            // 2. Fetch user profile with access token
            HttpHeaders profileHeaders = new HttpHeaders();
            profileHeaders.setBearerAuth(accessToken);
            HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);

            ResponseEntity<String> profileResponse = restTemplate.exchange(
                    "https://www.googleapis.com/oauth2/v3/userinfo", HttpMethod.GET, profileRequest, String.class
            );

            JsonNode profileJson = objectMapper.readTree(profileResponse.getBody());
            String email = profileJson.path("email").asText(null);
            boolean emailVerified = profileJson.path("email_verified").asBoolean(false);
            String name = profileJson.path("name").asText(null);
            String picture = profileJson.path("picture").asText(null);

            if (email == null || email.isBlank()) {
                throw new BadRequestException("Google did not provide an email address.");
            }

            if (!emailVerified) {
                throw new BadRequestException("The Google account's email is not verified by Google.");
            }

            return findOrCreateAndLoginUser(email, name, picture);

        } catch (HttpStatusCodeException ex) {
            logger.error("Google OAuth token exchange failed: status={} body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new BadRequestException("Failed to authenticate with Google: " + ex.getStatusCode());
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception ex) {
            logger.error("Unexpected error during Google OAuth processing", ex);
            throw new BadRequestException("Google authentication failed. Please try again.");
        }
    }

    @Transactional
    public AuthResponse processGoogleIdToken(String credential) {
        if (credential == null || credential.isBlank()) {
            throw new BadRequestException("Credential token is required.");
        }

        try {
            // Verify ID token via Google tokeninfo
            String tokenInfoUrl = "https://oauth2.googleapis.com/tokeninfo?id_token="
                    + URLEncoder.encode(credential, StandardCharsets.UTF_8);

            ResponseEntity<String> response = restTemplate.getForEntity(tokenInfoUrl, String.class);
            JsonNode payload = objectMapper.readTree(response.getBody());

            String email = payload.path("email").asText(null);
            boolean emailVerified = payload.path("email_verified").asBoolean(false)
                    || "true".equalsIgnoreCase(payload.path("email_verified").asText());
            String name = payload.path("name").asText(null);
            String picture = payload.path("picture").asText(null);

            if (email == null || email.isBlank()) {
                throw new BadRequestException("Google token did not contain an email address.");
            }

            if (!emailVerified) {
                throw new BadRequestException("The Google account's email is not verified by Google.");
            }

            return findOrCreateAndLoginUser(email, name, picture);

        } catch (HttpStatusCodeException ex) {
            logger.error("Google ID token validation failed: status={} body={}",
                    ex.getStatusCode(), ex.getResponseBodyAsString());
            throw new BadRequestException("Invalid Google credential.");
        } catch (BadRequestException bre) {
            throw bre;
        } catch (Exception ex) {
            logger.error("Error verifying Google ID token", ex);
            throw new BadRequestException("Google token authentication failed.");
        }
    }

    private AuthResponse findOrCreateAndLoginUser(String email, String name, String picture) {
        Optional<User> existingUserOpt = userRepository.findByEmail(email);
        User user;

        if (existingUserOpt.isPresent()) {
            user = existingUserOpt.get();
            // Google has verified this email - ensure emailVerified is true
            boolean modified = false;
            if (!user.isEmailVerified()) {
                user.setEmailVerified(true);
                modified = true;
            }
            if ((user.getAvatarUrl() == null || user.getAvatarUrl().isBlank()) && picture != null) {
                user.setAvatarUrl(picture);
                modified = true;
            }
            if (modified) {
                user = userRepository.save(user);
            }
        } else {
            // Create a new normal user account
            String baseUsername = generateBaseUsername(email, name);
            String username = baseUsername;
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter;
                counter++;
            }

            String displayName = (name != null && !name.isBlank()) ? name.trim() : username;

            user = new User(
                    displayName,
                    username,
                    email,
                    passwordEncoder.encode(UUID.randomUUID().toString()),
                    Role.ROLE_USER
            );
            user.setEmailVerified(true);
            user.setAuthProvider("GOOGLE");
            if (picture != null && !picture.isBlank()) {
                user.setAvatarUrl(picture);
            }
            user = userRepository.save(user);
            logger.info("Created new Google-authenticated user id={}, username={}", user.getId(), user.getUsername());
        }

        // Authenticate and issue existing platform JWT
        UserPrincipal principal = UserPrincipal.create(user);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        String jwt = tokenProvider.generateToken(authentication);

        AuthResponse authResponse = new AuthResponse(
                jwt,
                user.getId(),
                user.getName(),
                user.getUsername(),
                user.getEmail(),
                user.getRole().name()
        );
        authResponse.setBio(user.getBio());
        authResponse.setSkills(user.getSkills());
        authResponse.setGithubUrl(user.getGithubUrl());
        authResponse.setLinkedinUrl(user.getLinkedinUrl());
        authResponse.setAvatarUrl(user.getAvatarUrl());
        authResponse.setEmailVerified(true);
        authResponse.setMessage("Google authentication successful.");

        return authResponse;
    }

    private String generateBaseUsername(String email, String name) {
        String base = null;
        if (email != null && email.contains("@")) {
            base = email.substring(0, email.indexOf('@'));
        } else if (name != null && !name.isBlank()) {
            base = name.toLowerCase().replaceAll("[^a-z0-9]", "");
        }
        if (base == null || base.isBlank()) {
            base = "user";
        }
        base = base.replaceAll("[^a-zA-Z0-9_]", "").toLowerCase();
        if (base.length() > 30) {
            base = base.substring(0, 30);
        }
        if (base.length() < 3) {
            base = base + "123";
        }
        return base;
    }
}

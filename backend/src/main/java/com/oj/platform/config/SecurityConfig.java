package com.oj.platform.config;

import com.oj.platform.security.JwtAuthenticationEntryPoint;
import com.oj.platform.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler, JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> {})
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers("/api/health", "/api/auth/**").permitAll()
                .requestMatchers("/api/translation/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/problems", "/api/problems/{id}").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/leaderboard").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/submissions/run").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/certificates/verify/**").permitAll()

                // Saved-code sub-resource under /api/problems/{id}/saved-code is a
                // per-student feature, not admin content management - must be matched
                // (and thus resolved) BEFORE the broader admin-only /api/problems/**
                // PUT/POST rules below, since Spring Security uses the first match.
                .requestMatchers(HttpMethod.GET, "/api/problems/*/saved-code").authenticated()
                .requestMatchers(HttpMethod.PUT, "/api/problems/*/saved-code").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/problems/*/saved-code").authenticated()

                // Admin specific endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/problems/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/problems/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/problems/**").hasRole("ADMIN")
                .requestMatchers("/api/testcases/**").hasRole("ADMIN")
                .requestMatchers("/api/admin-certificates/**").hasRole("ADMIN")

                // Authenticated user endpoints
                .requestMatchers(HttpMethod.POST, "/api/translation/translate").authenticated()
                .requestMatchers("/api/users/**", "/api/submissions/**", "/api/certificates/**", "/api/ai/**", "/api/translation/**").authenticated()
                
                .anyRequest().authenticated()
            );

        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

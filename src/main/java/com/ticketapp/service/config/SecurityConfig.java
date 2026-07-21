package com.ticketapp.service.config;

import com.ticketapp.service.security.JwtAuthFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
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
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Configures the main security filter chain for HTTP requests.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // 1. Disable CSRF (Cross-Site Request Forgery)
                // Why: CSRF attacks rely on browser cookies (sessions). Since we are using
                // stateless JWT tokens instead of sessions, we don't need CSRF protection.
                .csrf(AbstractHttpConfigurer::disable)

                // 2. Configure which endpoints require authentication
                .authorizeHttpRequests(auth -> auth
                        // Allow anyone to access the login and registration endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        // Any other request must be authenticated (have a valid token)
                        .anyRequest().authenticated()
                )

                // 3. Set session management to STATELESS
                // Why: REST APIs should be stateless. We don't want Spring Security to
                // create an HTTP session in the server memory. Every request must be 
                // authenticated independently using the JWT token.
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 4. Add our custom JWT filter
                // Why: Spring Security normally uses UsernamePasswordAuthenticationFilter to check
                // for standard login form submissions. We want our JwtAuthFilter to run BEFORE that,
                // so it can check for the JWT token and log the user in automatically if the token is valid.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                .build();
    }

    /**
     * Provides the PasswordEncoder bean.
     * Why: We never store plain-text passwords in the database. BCrypt securely hashes
     * the passwords, and Spring Security will use this bean to compare hashed passwords during login.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Provides the AuthenticationManager bean.
     * Why: This is the core component of Spring Security that processes authentication requests.
     * We expose it as a bean so we can inject it into our AuthController later to manually trigger logins.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}

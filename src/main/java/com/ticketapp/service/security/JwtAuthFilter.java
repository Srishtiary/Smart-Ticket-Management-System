package com.ticketapp.service.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filter that intercepts every HTTP request to check for a valid JWT token.
 * It extends OncePerRequestFilter to ensure it only runs once per request.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService customUserDetailsService;

    public JwtAuthFilter(JwtUtil jwtUtil, CustomUserDetailsService customUserDetailsService) {
        this.jwtUtil = jwtUtil;
        this.customUserDetailsService = customUserDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Get the Authorization header from the incoming request
        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        // 2. Check if the header exists and starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            // Extract the token by removing the "Bearer " prefix (first 7 characters)
            token = authHeader.substring(7);
            
            // Try to extract the email (username) from the token
            try {
                email = jwtUtil.extractEmail(token);
            } catch (Exception e) {
                // If token extraction fails (e.g. invalid format), we just catch it 
                // and the email remains null. The filter chain will continue.
            }
        }

        // 3. If we successfully found an email, and there is no authentication in the context yet
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // Load the user details from the database
            UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);

            // 4. Validate the token using our JwtUtil
            if (jwtUtil.validateToken(token)) {
                
                // 5. Create an Authentication object for Spring Security
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, 
                        null, 
                        userDetails.getAuthorities()
                );
                
                // Attach additional details about the web request (like IP address, session ID)
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 6. Finally, set this authenticated user in the Security Context.
                // This tells Spring Security: "This user is logged in and authenticated for this request."
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 7. Pass the request and response to the next filter in the chain.
        // If the token was missing or invalid, the request continues without authentication,
        // and Spring Security will block it later if the endpoint requires login.
        filterChain.doFilter(request, response);
    }
}

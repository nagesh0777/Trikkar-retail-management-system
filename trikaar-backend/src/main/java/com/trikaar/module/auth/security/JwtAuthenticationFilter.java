package com.trikaar.module.auth.security;

import com.trikaar.shared.context.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * JWT authentication filter that:
 * 1. Extracts Bearer token from Authorization header
 * 2. Validates the token
 * 3. Sets SecurityContext authentication
 * 4. Populates TenantContext with businessId and userId
 *
 * CRITICAL: Clears TenantContext in finally block to prevent cross-request
 * leakage.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = extractJwtFromRequest(request);

            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                String username = jwtTokenProvider.extractUsername(jwt);
                UUID businessId = jwtTokenProvider.extractBusinessId(jwt);
                UUID userId = jwtTokenProvider.extractUserId(jwt);
                String role = jwtTokenProvider.extractRole(jwt);

                // Set tenant context for downstream services and repositories
                TenantContext.setBusinessId(businessId);
                TenantContext.setUserId(userId);

                // Build authentication token with authorities
                List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(username,
                        null, authorities);
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);

                // Add to MDC for structured logging
                org.slf4j.MDC.put("businessId", businessId.toString());
                org.slf4j.MDC.put("userId", userId.toString());

                log.debug("Authenticated user '{}' for business '{}'", username, businessId);
            }

            filterChain.doFilter(request, response);
        } finally {
            // CRITICAL: Prevent tenant context leakage in thread-pooled environments
            TenantContext.clear();
            org.slf4j.MDC.remove("businessId");
            org.slf4j.MDC.remove("userId");
        }
    }

    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

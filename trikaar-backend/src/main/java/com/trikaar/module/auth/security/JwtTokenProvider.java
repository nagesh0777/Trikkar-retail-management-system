package com.trikaar.module.auth.security;

import com.trikaar.config.SecurityProperties;
import com.trikaar.shared.enums.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * JWT token provider handling token generation, validation, and claim
 * extraction.
 * Tokens include businessId as a custom claim for multi-tenant enforcement.
 */
@Slf4j
@Service
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final SecurityProperties securityProperties;

    public JwtTokenProvider(SecurityProperties securityProperties) {
        this.securityProperties = securityProperties;
        byte[] keyBytes = Decoders.BASE64.decode(securityProperties.getJwt().getSecretKey());
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Generate access token with user identity and tenant context.
     */
    public String generateAccessToken(UUID userId, String username, UUID businessId, Role role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("businessId", businessId.toString());
        claims.put("role", role.name());
        claims.put("userId", userId.toString());

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + securityProperties.getJwt().getAccessTokenExpirationMs()))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Generate refresh token (minimal claims, longer expiry).
     */
    public String generateRefreshToken(UUID userId, UUID businessId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("businessId", businessId.toString());
        claims.put("userId", userId.toString());
        claims.put("type", "refresh");

        return Jwts.builder()
                .claims(claims)
                .subject(userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()
                        + securityProperties.getJwt().getRefreshTokenExpirationMs()))
                .signWith(signingKey)
                .compact();
    }

    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    public UUID extractBusinessId(String token) {
        String businessId = extractAllClaims(token).get("businessId", String.class);
        return UUID.fromString(businessId);
    }

    public UUID extractUserId(String token) {
        String userId = extractAllClaims(token).get("userId", String.class);
        return UUID.fromString(userId);
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            log.warn("JWT token expired: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
        } catch (SecurityException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty: {}", ex.getMessage());
        }
        return false;
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

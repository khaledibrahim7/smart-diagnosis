package com.khaled.smart_diagnosis.security;

import com.khaled.smart_diagnosis.exception.InvalidTokenException;
import com.khaled.smart_diagnosis.exception.TokenExpiredException;
import com.khaled.smart_diagnosis.exception.TokenGenerationException;
import com.khaled.smart_diagnosis.exception.TokenProcessingException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import io.jsonwebtoken.security.SignatureException;

@Slf4j
@Component
public class JWTUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (IllegalArgumentException e) {
            log.error("Invalid secret key format", e);
            throw new IllegalStateException("Invalid secret key format", e);
        }
    }

    /**
     * Generate JWT token with subject and expiration time.
     *
     * @param username the username for the token subject
     * @return generated JWT token
     * @throws TokenGenerationException if token generation fails
     */
    public String generateToken(String username) {
        try {
            Date now = new Date();
            Date expiryDate = new Date(now.getTime() + expiration);

            return Jwts.builder()
                    .setSubject(username)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            log.error("Failed to generate token for user: {}", username, e);
            throw new TokenGenerationException("Failed to generate token", e);
        }
    }

    /**
     * Extract username from JWT token.
     *
     * @param token JWT token to extract the username from
     * @return username extracted from the token
     * @throws TokenExpiredException   if token is expired
     * @throws InvalidTokenException   if token is invalid
     * @throws TokenProcessingException if error occurs while processing token
     */
    public String extractUsername(String token) {
        try {
            return parseToken(token).getBody().getSubject();
        } catch (ExpiredJwtException e) {
            log.warn("Token expired when extracting username: {}", token, e);
            throw new TokenExpiredException("Token has expired", e);
        } catch (JwtException e) {
            log.error("Error extracting username from token: {}", token, e);
            throw new InvalidTokenException("Invalid JWT token", e);
        } catch (Exception e) {
            log.error("Error processing the token: {}", token, e);
            throw new TokenProcessingException("Error processing the token", e);
        }
    }

    /**
     * Validate JWT token's authenticity and expiration.
     *
     * @param token JWT token to validate
     * @return true if token is valid, false otherwise
     * @throws InvalidTokenException if token signature is invalid or token is invalid
     * @throws TokenExpiredException if token is expired
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);  // Verifies both expiration and signature
            log.info("Token validation successful");
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", token, e);
            throw new TokenExpiredException("Token has expired", e);
        } catch (SignatureException e) {
            log.error("Invalid token signature: {}", token, e);
            throw new InvalidTokenException("Invalid JWT signature", e);
        } catch (JwtException e) {
            log.error("Token validation failed: {}", token, e);
            throw new InvalidTokenException("Invalid JWT token", e);
        } catch (Exception e) {
            log.error("Unexpected error during token validation: {}", token, e);
            throw new TokenProcessingException("Error processing the token during validation", e);
        }
    }

    /**
     * Parse JWT token to extract claims.
     *
     * @param token JWT token to parse
     * @return claims extracted from the token
     */
    private Jws<Claims> parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
        } catch (Exception e) {
            log.error("Failed to parse token: {}", token, e);
            throw new TokenProcessingException("Error parsing token", e);
        }
    }
}

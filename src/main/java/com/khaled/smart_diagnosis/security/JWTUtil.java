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

    /**
     * Retrieve the signing key used to sign the JWT token.
     *
     * @return The signing key for JWT.
     */
    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);
            log.info("Decoded Key Length: {}", key.getEncoded().length);
            return key;
        } catch (IllegalArgumentException e) {
            log.error("Invalid secret key format!", e);
            throw new IllegalStateException("Invalid secret key format!", e);
        }
    }

    /**
     * Generate JWT token for the given username.
     *
     * @param username The username to be embedded in the token.
     * @return The generated JWT token.
     */
    public String generateToken(String username) {
        log.info("Generating token for user: {}", username);
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Extract the username from the JWT token.
     *
     * @param token The JWT token from which the username is extracted.
     * @return The extracted username.
     */
    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            log.error("Error extracting username from token: {}", token, e);
            throw new InvalidTokenException("Invalid JWT token!", e);
        }
    }

    /**
     * Validate the JWT token to check its integrity and expiration.
     *
     * @param token The JWT token to be validated.
     * @return True if the token is valid, otherwise false.
     */
    public boolean validateToken(String token) {
        try {
            log.info("Validating Token: {}", token);
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token); // This will throw an exception if the token is invalid or expired
            log.info("Token validation successful for token: {}", token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired: {}", token, e);
            throw new TokenExpiredException("Token has expired!", e);
        } catch (SignatureException e) {
            log.error("Token signature invalid: {}", token, e);
            throw new InvalidTokenException("Invalid JWT signature!", e);
        } catch (JwtException e) {
            log.error("JWT validation failed for token: {}", token, e);
            throw new InvalidTokenException("Invalid JWT token!", e);
        }
    }
}

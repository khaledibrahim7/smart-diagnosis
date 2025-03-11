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

    // تحويل المفتاح إلى Base64 لضمان صحته
    private Key getSigningKey() {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(secretKey);
            Key key = Keys.hmacShaKeyFor(keyBytes);

            log.info("Decoded Key Length: " + key.getEncoded().length); 

            return key;
        } catch (IllegalArgumentException e) {
            log.error("Invalid secret key format!", e);
            throw new IllegalStateException("Invalid secret key format!", e);
        }
    }



    // توليد التوكن
    public String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException e) {
            log.error("Error extracting username from token!", e);
            throw new InvalidTokenException("Invalid JWT token!", e);
        }
    }


    public boolean validateToken(String token) {
        try {
            log.info("Validating Token: " + token);
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Token expired!", e);
            throw new TokenExpiredException("Token has expired!", e);
        } catch (JwtException e) {
            log.error("JWT validation failed!", e);
            throw new InvalidTokenException("Invalid JWT token!", e);
        }
    }
}

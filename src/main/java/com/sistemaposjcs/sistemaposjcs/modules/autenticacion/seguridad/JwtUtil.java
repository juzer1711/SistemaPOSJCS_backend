package com.sistemaposjcs.sistemaposjcs.modules.autenticacion.seguridad;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.time.Duration;
import java.util.UUID;

@Component
public class JwtUtil {

    private final SecretKey signingKey;
    private final Duration expiration;

    public JwtUtil(
            @Value("${JWT_SECRET}") String secret,
            @Value("${app.security.jwt.expiration:PT1H}") Duration expiration
    ) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expiration = expiration;
    }

    // Generar token
    public String generateToken(String username) {
        Date issuedAt = new Date();
        return Jwts.builder()
                .subject(username)
                .id(UUID.randomUUID().toString())
                .issuedAt(issuedAt)
                .expiration(Date.from(issuedAt.toInstant().plus(expiration)))
                .signWith(signingKey, Jwts.SIG.HS256)
                .compact();
    }

    // Extraer claims del token
    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public String extractUsername(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractTokenId(String token) {
        return extractClaims(token).getId();
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractClaims(token);
            return claims.getExpiration() != null
                    && claims.getExpiration().after(new Date())
                    && claims.getSubject() != null
                    && claims.getId() != null;
        } catch (Exception e) {
            return false;
        }
    }
}
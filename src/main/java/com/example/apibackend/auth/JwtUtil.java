package com.example.apibackend.auth;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${security.jwt.secret}")
    private String secret;
    @Value("${security.jwt.issuer}")
    private String issuer;
    @Value("${security.jwt.accessTokenTtlMinutes:15}")
    private int ttlMinutes;

    /**
     * Create a JWT token for the user. Keys and TTL are configured in application.yml.
     */
    public String createToken(Long userId, String email, String role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .setIssuer(issuer)
                .claim("userId", userId)
                .claim("role", role)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMinutes * 60_000L))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    /**
     * Validate and parse JWT token. Throws if invalid or expired.
     */
    public Claims validateToken(String token) throws JwtException {
        return Jwts.parser()
                .setSigningKey(secret)
                .parseClaimsJws(token)
                .getBody();
    }
}


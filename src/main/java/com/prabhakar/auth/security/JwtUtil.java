package com.prabhakar.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey key;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String base64Secret) {

        if (base64Secret == null || base64Secret.isBlank()) {
            throw new IllegalStateException("Missing configuration property 'jwt.secret'. Please provide a Base64 encoded key.");
        }

        byte[] keyBytes = Decoders.BASE64.decode(base64Secret);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must be at least 32 bytes when Base64 decoded.");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    // 🔥 Generate Access Token Only
    public String generateToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException ex) {
            return false;
        }
    }
}

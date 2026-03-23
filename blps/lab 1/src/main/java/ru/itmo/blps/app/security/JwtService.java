package ru.itmo.blps.app.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.itmo.blps.app.models.AppUser;

@Service
public class JwtService {
    private final String jwtSecret;
    private final long jwtExpirationHours;

    public JwtService(@Value("${app.security.jwt.secret}") String jwtSecret,
                      @Value("${app.security.jwt.expiration-hours}") long jwtExpirationHours) {
        this.jwtSecret = jwtSecret;
        this.jwtExpirationHours = jwtExpirationHours;
    }

    public String generateToken(AppUser user) {
        return generateToken(user.getUsername(), user.getId(), user.getRole().name(), user.getParticipantId());
    }

    public String generateToken(String username, Long userId, String role, Long participantId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationHours * 3600 * 1000);

        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        JwtBuilder builder = Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("userId", userId)
                .claim("role", role)
                .claim("participantId", participantId)
                .signWith(key);

        return builder.compact();
    }

    public String extractUsername(String token) {
        Claims claims = extractAllClaims(token);
        return claims.getSubject();
    }

    public Long extractUserId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("userId", Long.class);
    }

    public String extractRole(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("role", String.class);
    }

    public Long extractParticipantId(String token) {
        Claims claims = extractAllClaims(token);
        return claims.get("participantId", Long.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims extractAllClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}



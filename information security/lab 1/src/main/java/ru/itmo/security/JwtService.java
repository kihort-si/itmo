package ru.itmo.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Optional;

public final class JwtService {
    public static final long TOKEN_LIFETIME_SECONDS = 900;
    private static final String ISSUER = "secure-api";

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(String secret) {
        this.algorithm = Algorithm.HMAC256(secret);
        this.verifier = JWT.require(algorithm).withIssuer(ISSUER).build();
    }

    public String issue(String subject) {
        Instant now = Instant.now();
        return JWT.create()
                .withIssuer(ISSUER)
                .withSubject(subject)
                .withIssuedAt(Date.from(now))
                .withExpiresAt(Date.from(now.plus(TOKEN_LIFETIME_SECONDS, ChronoUnit.SECONDS)))
                .sign(algorithm);
    }

    public Optional<String> verify(String token) {
        try {
            return Optional.ofNullable(verifier.verify(token).getSubject());
        } catch (JWTVerificationException exception) {
            return Optional.empty();
        }
    }
}

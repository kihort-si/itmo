package web.weblab4.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import web.weblab4.database.UserDB;

import java.util.Base64;
import java.util.Date;

@Stateless
public class JwtManager {
    private static final byte[] SECRET_KEY = Base64.getDecoder().decode(System.getenv("TOKEN_SECRET"));
    private static final Algorithm ALGORITHM = Algorithm.HMAC256(SECRET_KEY);
    private static final long EXPIRATION_TIME = 900000;
    private static final long REFRESH_EXPIRATION_TIME = 864000000;

    @EJB
    private UserDB userDB;

    public String generateAccessToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withClaim("type", "access")
                .sign(ALGORITHM);
    }

    public String generateRefreshToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + REFRESH_EXPIRATION_TIME))
                .withClaim("type", "refresh")
                .sign(ALGORITHM);
    }

    public String refreshAccessToken(String refreshToken) {
        try {
            DecodedJWT decodedJWT = JWT.require(ALGORITHM)
                    .build()
                    .verify(refreshToken);

            String username = decodedJWT.getSubject();

            if (!isValidRefreshToken(refreshToken, username)) {
                throw new RuntimeException("Invalid refresh token");
            }

            return generateAccessToken(username);

        } catch (JWTVerificationException e) {
            throw new RuntimeException("Refresh token verification failed", e);
        }
    }

    private boolean isValidRefreshToken(String refreshToken, String email) {
        String token = userDB.getRefreshToken(email);
        return token.equals(refreshToken);
    }
}

package com.vt.service

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.exceptions.JWTVerificationException
import com.vt.model.AccessPrincipal
import com.vt.model.AuthenticationException
import io.ktor.server.config.ApplicationConfig
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Duration
import java.time.Instant
import java.util.*

object JwtService {
    private lateinit var algorithm: Algorithm
    private lateinit var verifier: JWTVerifier
    private lateinit var issuer: String
    private lateinit var audience: String
    private lateinit var accessTtl: Duration
    private lateinit var refreshTtl: Duration

    fun init(config: ApplicationConfig) {
        val jwtConfig = config.config("ktor.jwt")
        issuer = jwtConfig.property("issuer").getString()
        audience = jwtConfig.property("audience").getString()
        accessTtl = Duration.ofMinutes(jwtConfig.property("accessTtlMinutes").getString().toLong())
        refreshTtl = Duration.ofDays(jwtConfig.property("refreshTtlDays").getString().toLong())

        val privateKey = loadPrivateKey(jwtConfig.property("privateKeyPath").getString())
        val publicKey = loadPublicKey(jwtConfig.property("publicKeyPath").getString())

        algorithm = Algorithm.RSA256(publicKey, privateKey)
        verifier = JWT.require(algorithm)
            .withIssuer(issuer)
            .withAudience(audience)
            .withClaim("typ", "access")
            .build()
    }

    fun accessTokenTtl(): Duration = accessTtl
    fun refreshTokenTtl(): Duration = refreshTtl

    fun createAccessToken(
        userId: Int,
        email: String,
        username: String,
        clntId: Int,
        roles: List<String>,
        sessionId: UUID
    ): String {
        val now = Instant.now()
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withClaim("email", email)
            .withClaim("username", username)
            .withClaim("clntId", clntId)
            .withClaim("roles", roles)
            .withClaim("sid", sessionId.toString())
            .withClaim("typ", "access")
            .withIssuedAt(Date.from(now))
            .withExpiresAt(Date.from(now.plus(accessTtl)))
            .sign(algorithm)
    }

    fun requirePrincipal(authorizationHeader: String?): AccessPrincipal {
        val token = extractBearerToken(authorizationHeader)
        try {
            val decoded = verifier.verify(token)
            val userId = decoded.subject?.toIntOrNull()
                ?: throw AuthenticationException("Invalid token subject")
            val email = decoded.getClaim("email").asString()
                ?: throw AuthenticationException("Token email is missing")
            val username = decoded.getClaim("username").asString()
                ?: throw AuthenticationException("Token username is missing")
            val clntId = decoded.getClaim("clntId").asInt()
                ?: throw AuthenticationException("Token clntId is missing")
            val sessionId = decoded.getClaim("sid").asString()
                ?.let(UUID::fromString)
                ?: throw AuthenticationException("Token session is missing")
            val roles = decoded.getClaim("roles").asList(String::class.java) ?: emptyList()
            return AccessPrincipal(
                userId = userId,
                email = email,
                username = username,
                clntId = clntId,
                roles = roles,
                sessionId = sessionId
            )
        } catch (_: JWTVerificationException) {
            throw AuthenticationException("Invalid or expired access token")
        }
    }

    private fun extractBearerToken(authorizationHeader: String?): String {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw AuthenticationException("Missing bearer token")
        }
        return authorizationHeader.removePrefix("Bearer ").trim()
    }

    private fun loadPrivateKey(path: String): RSAPrivateKey {
        val pem = loadPem(path)
        val keyBytes = decodePem(pem, "PRIVATE KEY")
        val spec = PKCS8EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePrivate(spec) as RSAPrivateKey
    }

    private fun loadPublicKey(path: String): RSAPublicKey {
        val pem = loadPem(path)
        val keyBytes = decodePem(pem, "PUBLIC KEY")
        val spec = X509EncodedKeySpec(keyBytes)
        return KeyFactory.getInstance("RSA").generatePublic(spec) as RSAPublicKey
    }

    private fun loadPem(path: String): String {
        return if (path.startsWith("classpath:")) {
            val resourcePath = path.removePrefix("classpath:").let {
                if (it.startsWith("/")) it else "/$it"
            }
            JwtService::class.java.getResourceAsStream(resourcePath)
                ?.readBytes()
                ?.toString(StandardCharsets.UTF_8)
                ?: throw IllegalStateException("Resource not found: $path")
        } else {
            Files.readString(Paths.get(path))
        }
    }

    private fun decodePem(pem: String, type: String): ByteArray {
        val normalized = pem
            .replace("-----BEGIN $type-----", "")
            .replace("-----END $type-----", "")
            .replace("\\s".toRegex(), "")
        return Base64.getDecoder().decode(normalized)
    }
}
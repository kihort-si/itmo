package com.vt.model

import java.time.Instant
import java.util.UUID

enum class UserStatus {
    PENDING_PROVISIONING,
    ACTIVE,
    DISABLED
}

enum class SessionStatus {
    ACTIVE,
    REVOKED,
    EXPIRED
}

data class RegisterRequest(
    val email: String,
    val password: String,
    val username: String,
    val clntId: Int
)

data class ChangeStatusRequest(
    val userId: Int,
    val status: String
)

data class LoginRequest(
    val login: String,   // может быть email или username
    val password: String
)

data class RefreshRequest(
    val refreshToken: String
)

data class RegisterResponse(
    val userId: Int,
    val email: String,
    val username: String,
    val clntId: Int,
    val status: UserStatus,
    val roles: List<String>
)

data class UserResponse(
    val userId: Int,
    val email: String,
    val username: String,
    val clntId: Int,
    val status: UserStatus,
    val roles: List<String>,
    val sessionId: UUID? = null
)

data class TokenPairResponse(
    val accessToken: String,
    val refreshToken: String,
    val sessionId: UUID,
    val user: UserResponse
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String
)

data class HealthResponse(
    val status: String
)

data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

data class AuthUser(
    val userId: Int,
    val email: String,
    val username: String,
    val clntId: Int,
    val passwordHash: String,
    val status: UserStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class AuthSession(
    val sessionId: UUID,
    val userId: Int,
    val refreshTokenHash: String,
    val status: SessionStatus,
    val createdAt: Instant,
    val lastUsedAt: Instant,
    val expiresAt: Instant,
    val revokedAt: Instant?,
    val ip: String?,
    val userAgent: String?
)

data class AccessPrincipal(
    val userId: Int,
    val email: String,
    val username: String,
    val clntId: Int,
    val roles: List<String>,
    val sessionId: UUID
)

class AuthenticationException(message: String) : RuntimeException(message)
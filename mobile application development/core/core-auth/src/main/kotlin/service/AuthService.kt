package com.vt.service

import com.vt.dao.AuthSessionDao
import com.vt.dao.AuthUserDao
import com.vt.model.*
import org.slf4j.LoggerFactory
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.UUID

object AuthService {
    private val logger = LoggerFactory.getLogger(AuthService::class.java)
    private val random = SecureRandom()

    private lateinit var jwtService: JwtService
    private lateinit var provisioningStubService: ProvisioningStubService

    fun init(jwtService: JwtService, provisioningStubService: ProvisioningStubService) {
        this.jwtService = jwtService
        this.provisioningStubService = provisioningStubService
    }

    fun register(request: RegisterRequest): RegisterResponse {
        val email = normalizeEmail(request.email)
        val username = request.username.trim()
        require(username.isNotBlank()) { "Username is required" }

        if (AuthUserDao.findByEmail(email) != null) {
            throw IllegalStateException("User with email already exists")
        }
        if (AuthUserDao.findByUsername(username) != null) {
            throw IllegalStateException("Username already exists")
        }
        if (AuthUserDao.findByClntId(request.clntId) != null) {
            throw IllegalStateException("Client ID already exists")
        }

        val user = AuthUserDao.create(
            email = email,
            username = username,
            clntId = request.clntId,
            passwordHash = PasswordService.hash(request.password),
            status = UserStatus.ACTIVE   // статус по умолчанию ACTIVE
        )
        AuthUserDao.assignRole(user.userId, "USER")
        provisioningStubService.onUserRegistered(user.userId, user.email)

        return RegisterResponse(
            userId = user.userId,
            email = user.email,
            username = user.username,
            clntId = user.clntId,
            status = user.status,
            roles = AuthUserDao.getRoles(user.userId)
        )
    }

    fun login(request: LoginRequest, userAgent: String?, ip: String?): TokenPairResponse {
        val login = request.login.trim()
        require(login.isNotBlank()) { "Login is required" }

        val user = AuthUserDao.findByLogin(login)
            ?: throw AuthenticationException("Invalid login or password")

        if (!PasswordService.verify(request.password, user.passwordHash)) {
            throw AuthenticationException("Invalid login or password")
        }

        if (user.status != UserStatus.ACTIVE) {
            throw AuthenticationException("User account is not active")
        }

        val roles = AuthUserDao.getRoles(user.userId)
        val sessionId = UUID.randomUUID()
        val refreshToken = generateRefreshToken(sessionId)
        val session = AuthSessionDao.create(
            sessionId = sessionId,
            userId = user.userId,
            refreshTokenHash = sha256Hex(refreshToken),
            expiresAt = Instant.now().plus(jwtService.refreshTokenTtl()),
            ip = ip,
            userAgent = userAgent
        )

        return buildTokenPair(user, roles, session, refreshToken)
    }

    fun refresh(request: RefreshRequest): RefreshResponse {
        val sessionId = parseSessionId(request.refreshToken)
        val session = AuthSessionDao.findById(sessionId)
            ?: throw AuthenticationException("Refresh session not found")
        ensureRefreshSessionValid(session, request.refreshToken)

        val user = AuthUserDao.findById(session.userId)
            ?: throw AuthenticationException("User not found")
        if (user.status != UserStatus.ACTIVE) {
            throw AuthenticationException("User account is not active")
        }

        val roles = AuthUserDao.getRoles(user.userId)
        val nextRefreshToken = generateRefreshToken(sessionId)
        AuthSessionDao.rotateRefreshToken(
            sessionId = sessionId,
            refreshTokenHash = sha256Hex(nextRefreshToken),
            expiresAt = Instant.now().plus(jwtService.refreshTokenTtl())
        )

        return RefreshResponse(
            accessToken = jwtService.createAccessToken(
                userId = user.userId,
                email = user.email,
                username = user.username,
                clntId = user.clntId,
                roles = roles,
                sessionId = sessionId
            ),
            refreshToken = nextRefreshToken
        )
    }

    fun logout(principal: AccessPrincipal) {
        AuthSessionDao.revoke(principal.sessionId)
        logger.info("Logged out session {}", principal.sessionId)
    }

    fun me(principal: AccessPrincipal): UserResponse {
        val user = AuthUserDao.findById(principal.userId)
            ?: throw AuthenticationException("User not found")
        return UserResponse(
            userId = user.userId,
            email = user.email,
            username = user.username,
            clntId = user.clntId,
            status = user.status,
            roles = AuthUserDao.getRoles(user.userId),
            sessionId = principal.sessionId
        )
    }

    fun getUserById(userId: Int): UserResponse {
        val user = AuthUserDao.findById(userId)
            ?: throw NoSuchElementException("User not found with id=$userId")
        return UserResponse(
            userId = user.userId,
            email = user.email,
            username = user.username,
            clntId = user.clntId,
            status = user.status,
            roles = AuthUserDao.getRoles(user.userId)
        )
    }

    fun getUserByClntId(clntId: Int): UserResponse {
        val user = AuthUserDao.findByClntId(clntId)
            ?: throw NoSuchElementException("User not found with clntId=$clntId")
        return UserResponse(
            userId = user.userId,
            email = user.email,
            username = user.username,
            clntId = user.clntId,
            status = user.status,
            roles = AuthUserDao.getRoles(user.userId)
        )
    }

    fun changeUserStatus(request: ChangeStatusRequest): UserResponse {
        val newStatus = try {
            UserStatus.valueOf(request.status.uppercase())
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid status: ${request.status}. Allowed values: ${UserStatus.values().joinToString()}")
        }
        val updated = AuthUserDao.updateStatus(request.userId, newStatus)
        if (!updated) {
            throw NoSuchElementException("User not found with id=${request.userId}")
        }
        return getUserById(request.userId)  // возвращает актуальную информацию
    }

    fun deleteUserByClntId(clntId: Int) {
        val deleted = AuthUserDao.deleteByClntId(clntId)
        if (!deleted) {
            throw NoSuchElementException("User not found with clntId=$clntId")
        }
    }

    private fun buildTokenPair(
        user: AuthUser,
        roles: List<String>,
        session: AuthSession,
        refreshToken: String
    ): TokenPairResponse {
        return TokenPairResponse(
            accessToken = jwtService.createAccessToken(
                userId = user.userId,
                email = user.email,
                username = user.username,
                clntId = user.clntId,
                roles = roles,
                sessionId = session.sessionId
            ),
            refreshToken = refreshToken,
            sessionId = session.sessionId,
            user = UserResponse(
                userId = user.userId,
                email = user.email,
                username = user.username,
                clntId = user.clntId,
                status = user.status,
                roles = roles,
                sessionId = session.sessionId
            )
        )
    }

    private fun ensureRefreshSessionValid(session: AuthSession, refreshToken: String) {
        if (session.status == SessionStatus.REVOKED) {
            throw AuthenticationException("Refresh session is revoked")
        }
        if (session.status == SessionStatus.EXPIRED || Instant.now().isAfter(session.expiresAt)) {
            AuthSessionDao.markExpired(session.sessionId)
            throw AuthenticationException("Refresh session is expired")
        }
        if (sha256Hex(refreshToken) != session.refreshTokenHash) {
            throw AuthenticationException("Refresh token is invalid")
        }
    }

    private fun normalizeEmail(email: String): String {
        val normalized = email.trim().lowercase()
        require(normalized.isNotBlank()) { "Email is required" }
        require(normalized.contains("@")) { "Email format is invalid" }
        return normalized
    }

    private fun generateRefreshToken(sessionId: UUID): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val secret = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        return "${sessionId}.${secret}"
    }

    private fun parseSessionId(refreshToken: String): UUID {
        val dotIndex = refreshToken.indexOf('.')
        if (dotIndex <= 0) {
            throw AuthenticationException("Refresh token format is invalid")
        }
        return try {
            UUID.fromString(refreshToken.substring(0, dotIndex))
        } catch (_: IllegalArgumentException) {
            throw AuthenticationException("Refresh token session is invalid")
        }
    }

    private fun sha256Hex(value: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(value.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }
}
package com.vt.dao

import com.vt.model.AuthSession
import com.vt.model.AuthUser
import com.vt.model.SessionStatus
import com.vt.model.UserStatus
import com.vt.table.AuthRoleTable
import com.vt.table.AuthSessionTable
import com.vt.table.AuthUserRoleTable
import com.vt.table.AuthUserTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.time.Instant
import java.util.UUID

object AuthUserDao {
    fun create(
        email: String,
        username: String,
        clntId: Int,
        passwordHash: String,
        status: UserStatus
    ): AuthUser = transaction {
        val now = Instant.now()
        val userId = AuthUserTable.insert {
            it[AuthUserTable.email] = email
            it[AuthUserTable.username] = username
            it[AuthUserTable.clntId] = clntId
            it[AuthUserTable.passwordHash] = passwordHash
            it[AuthUserTable.status] = status.name
            it[createdAt] = now
            it[updatedAt] = now
        }[AuthUserTable.userId]
        findById(userId) ?: error("Failed to load created user")
    }

    fun findByEmail(email: String): AuthUser? = transaction {
        AuthUserTable.selectAll()
            .where { AuthUserTable.email eq email }
            .singleOrNull()
            ?.toAuthUser()
    }

    fun findByUsername(username: String): AuthUser? = transaction {
        AuthUserTable.selectAll()
            .where { AuthUserTable.username eq username }
            .singleOrNull()
            ?.toAuthUser()
    }

    fun findByClntId(clntId: Int): AuthUser? = transaction {
        AuthUserTable.selectAll()
            .where { AuthUserTable.clntId eq clntId }
            .singleOrNull()
            ?.toAuthUser()
    }

    /**
     * Ищет пользователя по логину, который может быть email или username.
     */
    fun findByLogin(login: String): AuthUser? = transaction {
        AuthUserTable.selectAll()
            .where { (AuthUserTable.email eq login) or (AuthUserTable.username eq login) }
            .singleOrNull()
            ?.toAuthUser()
    }

    fun findById(userId: Int): AuthUser? = transaction {
        AuthUserTable.selectAll()
            .where { AuthUserTable.userId eq userId }
            .singleOrNull()
            ?.toAuthUser()
    }

    fun assignRole(userId: Int, roleCode: String) = transaction {
        val roleId = AuthRoleTable.selectAll()
            .where { AuthRoleTable.code eq roleCode }
            .singleOrNull()
            ?.get(AuthRoleTable.roleId)
            ?: throw NoSuchElementException("Role not found: $roleCode")
        AuthUserRoleTable.insert {
            it[AuthUserRoleTable.userId] = userId
            it[AuthUserRoleTable.roleId] = roleId
        }
    }

    fun getRoles(userId: Int): List<String> = transaction {
        (AuthUserRoleTable innerJoin AuthRoleTable)
            .selectAll()
            .where { AuthUserRoleTable.userId eq userId }
            .map { it[AuthRoleTable.code] }
    }

    private fun ResultRow.toAuthUser(): AuthUser = AuthUser(
        userId = this[AuthUserTable.userId],
        email = this[AuthUserTable.email],
        username = this[AuthUserTable.username],
        clntId = this[AuthUserTable.clntId],
        passwordHash = this[AuthUserTable.passwordHash],
        status = UserStatus.valueOf(this[AuthUserTable.status]),
        createdAt = this[AuthUserTable.createdAt],
        updatedAt = this[AuthUserTable.updatedAt]
    )

    fun updateStatus(userId: Int, newStatus: UserStatus): Boolean = transaction {
        val updated = AuthUserTable.update({ AuthUserTable.userId eq userId }) {
            it[status] = newStatus.name
            it[updatedAt] = Instant.now()
        }
        updated > 0
    }

    fun deleteByClntId(clntId: Int): Boolean = transaction {
        val deleted = AuthUserTable.deleteWhere { AuthUserTable.clntId eq clntId }
        deleted > 0
    }
}

object AuthSessionDao {
    fun create(
        sessionId: UUID,
        userId: Int,
        refreshTokenHash: String,
        expiresAt: Instant,
        ip: String?,
        userAgent: String?
    ): AuthSession = transaction {
        val now = Instant.now()
        AuthSessionTable.insert {
            it[AuthSessionTable.sessionId] = sessionId
            it[AuthSessionTable.userId] = userId
            it[AuthSessionTable.refreshTokenHash] = refreshTokenHash
            it[status] = SessionStatus.ACTIVE.name
            it[createdAt] = now
            it[lastUsedAt] = now
            it[AuthSessionTable.expiresAt] = expiresAt
            it[AuthSessionTable.ip] = ip
            it[AuthSessionTable.userAgent] = userAgent
        }
        findById(sessionId) ?: error("Failed to load created session")
    }

    fun findById(sessionId: UUID): AuthSession? = transaction {
        AuthSessionTable.selectAll()
            .where { AuthSessionTable.sessionId eq sessionId }
            .singleOrNull()
            ?.toAuthSession()
    }

    fun rotateRefreshToken(sessionId: UUID, refreshTokenHash: String, expiresAt: Instant): AuthSession = transaction {
        val updated = AuthSessionTable.update({ AuthSessionTable.sessionId eq sessionId }) {
            it[AuthSessionTable.refreshTokenHash] = refreshTokenHash
            it[lastUsedAt] = Instant.now()
            it[AuthSessionTable.expiresAt] = expiresAt
            it[status] = SessionStatus.ACTIVE.name
        }
        if (updated == 0) throw NoSuchElementException("Session not found")
        findById(sessionId) ?: error("Failed to load rotated session")
    }

    fun revoke(sessionId: UUID) = transaction {
        val now = Instant.now()
        AuthSessionTable.update({
            (AuthSessionTable.sessionId eq sessionId) and (AuthSessionTable.status eq SessionStatus.ACTIVE.name)
        }) {
            it[status] = SessionStatus.REVOKED.name
            it[revokedAt] = now
            it[lastUsedAt] = now
        }
    }

    fun markExpired(sessionId: UUID) = transaction {
        AuthSessionTable.update({ AuthSessionTable.sessionId eq sessionId }) {
            it[status] = SessionStatus.EXPIRED.name
            it[revokedAt] = Instant.now()
            it[lastUsedAt] = Instant.now()
        }
    }

    private fun ResultRow.toAuthSession(): AuthSession = AuthSession(
        sessionId = this[AuthSessionTable.sessionId],
        userId = this[AuthSessionTable.userId],
        refreshTokenHash = this[AuthSessionTable.refreshTokenHash],
        status = SessionStatus.valueOf(this[AuthSessionTable.status]),
        createdAt = this[AuthSessionTable.createdAt],
        lastUsedAt = this[AuthSessionTable.lastUsedAt],
        expiresAt = this[AuthSessionTable.expiresAt],
        revokedAt = this[AuthSessionTable.revokedAt],
        ip = this[AuthSessionTable.ip],
        userAgent = this[AuthSessionTable.userAgent]
    )
}

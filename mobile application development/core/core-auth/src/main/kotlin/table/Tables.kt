package com.vt.table

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.Instant
import java.util.UUID

object AuthUserTable : Table("auth.auth_user") {
    val userId = integer("user_id").autoIncrement()
    val email = varchar("email", 255).uniqueIndex()
    val username = varchar("username", 255).uniqueIndex()
    val clntId = integer("clnt_id").uniqueIndex()
    val passwordHash = varchar("password_hash", 512)
    val status = varchar("status", 64)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val updatedAt = timestamp("updated_at").clientDefault { Instant.now() }

    override val primaryKey = PrimaryKey(userId)
}

object AuthRoleTable : Table("auth.auth_role") {
    val roleId = integer("role_id").autoIncrement()
    val code = varchar("code", 64).uniqueIndex()
    val name = varchar("name", 255)

    override val primaryKey = PrimaryKey(roleId)
}

object AuthUserRoleTable : Table("auth.auth_user_role") {
    val userRoleId = integer("user_role_id").autoIncrement()
    val userId = reference("user_id", AuthUserTable.userId, onDelete = ReferenceOption.CASCADE)
    val roleId = reference("role_id", AuthRoleTable.roleId, onDelete = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(userRoleId)

    init {
        uniqueIndex(userId, roleId)
    }
}

object AuthSessionTable : Table("auth.auth_session") {
    val sessionId = uuid("session_id").clientDefault { UUID.randomUUID() }
    val userId = reference("user_id", AuthUserTable.userId, onDelete = ReferenceOption.CASCADE)
    val refreshTokenHash = varchar("refresh_token_hash", 128)
    val status = varchar("status", 64)
    val createdAt = timestamp("created_at").clientDefault { Instant.now() }
    val lastUsedAt = timestamp("last_used_at").clientDefault { Instant.now() }
    val expiresAt = timestamp("expires_at")
    val revokedAt = timestamp("revoked_at").nullable()
    val ip = varchar("ip", 255).nullable()
    val userAgent = varchar("user_agent", 1024).nullable()

    override val primaryKey = PrimaryKey(sessionId)
}

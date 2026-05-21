package com.vt.dao

import com.vt.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import kotlin.let

object ClientDao {

    // Вспомогательная функция для преобразования исключения БД в бизнес-ошибку
    private fun handleUniqueViolation(e: SQLException, field: String): Nothing {
        throw IllegalStateException("$field must be unique: ${e.message}")
    }

    fun create(request: CreateClientRequest): ClientFullResponse = transaction {
        try {
            // Получаем ID статуса ACTIVE
            val activeStatusId = ClientStatusTable
                .select { ClientStatusTable.def eq "ACTIVE" }
                .single()[ClientStatusTable.clstId]

            val newClntId = ClientTable.insert {
                it[username] = request.username
                it[regionRefsIdentifier] = request.regionRefsIdentifier
                it[languageCode] = request.languageCode
                it[ClientTable.statusId] = activeStatusId   // <-- установка статуса
            }[ClientTable.clntId]

            ClientDetailsTable.insert {
                it[clntId] = newClntId
                it[fullName] = request.fullName
                it[email] = request.email
                request.phoneNumber?.let { phone -> it[phoneNumber] = phone }
                request.additionalInfo?.let { info -> it[additionalInfo] = info }
                request.profileExtension?.let { ext -> it[profileExtension] = ext }
            }

            findByClntId(newClntId)
                ?: throw IllegalStateException("Failed to retrieve created client")

        } catch (e: SQLException) {
            when {
                e.message?.contains("username", ignoreCase = true) == true ->
                    handleUniqueViolation(e, "username")
                e.message?.contains("email", ignoreCase = true) == true ->
                    handleUniqueViolation(e, "email")
                else -> throw IllegalStateException("Unique constraint violated: ${e.message}")
            }
        }
    }

    fun findByClntId(clntId: Int): ClientFullResponse? = transaction {
        (ClientTable innerJoin ClientDetailsTable innerJoin ClientStatusTable)
            .selectAll().where { ClientTable.clntId eq clntId }
            .map { row ->
                ClientFullResponse(
                    clntId = row[ClientTable.clntId],
                    username = row[ClientTable.username],
                    regionRefsIdentifier = row[ClientTable.regionRefsIdentifier],
                    createdAt = row[ClientTable.createdAt],
                    languageCode = row[ClientTable.languageCode],
                    status = row[ClientStatusTable.def],          // <-- статус строкой
                    fullName = row[ClientDetailsTable.fullName],
                    email = row[ClientDetailsTable.email],
                    phoneNumber = row[ClientDetailsTable.phoneNumber],
                    additionalInfo = row[ClientDetailsTable.additionalInfo],
                    profileExtension = row[ClientDetailsTable.profileExtension]
                )
            }.singleOrNull()
    }

    fun updateDetails(clntId: Int, request: UpdateClientDetailsRequest): ClientFullResponse = transaction {
        try {
            ClientDetailsTable.update({ ClientDetailsTable.clntId eq clntId }) { update ->
                request.fullName?.let { update[fullName] = it }
                request.email?.let { update[email] = it }
                if (request.phoneNumber != null) update[phoneNumber] = request.phoneNumber
                if (request.additionalInfo != null) update[additionalInfo] = request.additionalInfo
                if (request.profileExtension != null) update[profileExtension] = request.profileExtension
            }
            findByClntId(clntId) ?: throw NoSuchElementException("Client not found")
        } catch (e: SQLException) {
            if (e.message?.contains("email", ignoreCase = true) == true) {
                handleUniqueViolation(e, "email")
            } else {
                throw IllegalStateException("Unique constraint violated: ${e.message}")
            }
        }
    }

    fun updateSystemData(clntId: Int, request: UpdateSystemDataRequest): ClientFullResponse = transaction {
        try {
            ClientTable.update({ ClientTable.clntId eq clntId }) { update ->
                request.username?.let { update[username] = it }
                request.regionRefsIdentifier?.let { update[regionRefsIdentifier] = it }
                request.languageCode?.let { update[languageCode] = it }
                request.status?.let { newStatus ->
                    val newStatusId = ClientStatusTable
                        .select { ClientStatusTable.def eq newStatus }
                        .singleOrNull()?.get(ClientStatusTable.clstId)
                        ?: throw IllegalArgumentException("Unknown status: $newStatus")
                    update[ClientTable.statusId] = newStatusId   // ← явное указание колонки
                }
            }
            findByClntId(clntId) ?: throw NoSuchElementException("Client not found")
        } catch (e: SQLException) {
            if (e.message?.contains("username", ignoreCase = true) == true) {
                handleUniqueViolation(e, "username")
            } else {
                throw IllegalStateException("Unique constraint violated: ${e.message}")
            }
        }
    }

    fun exists(clntId: Int): Boolean = transaction {
        ClientTable.selectAll().where { ClientTable.clntId eq clntId }.any()
    }

    fun delete(clntId: Int): Boolean = transaction {
        ClientAttributeTable.deleteWhere { ClientAttributeTable.clntId eq clntId }
        ClientAccountTable.deleteWhere { ClientAccountTable.clntId eq clntId }
        ClientDetailsTable.deleteWhere { ClientDetailsTable.clntId eq clntId }
        ClientTable.deleteWhere { ClientTable.clntId eq clntId } > 0
    }

    fun isUsernameInUse(username: String): Boolean = transaction {
        ClientTable.select { ClientTable.username eq username }.any()
    }

    fun isEmailInUse(email: String): Boolean = transaction {
        ClientDetailsTable.select { ClientDetailsTable.email eq email }.any()
    }

}
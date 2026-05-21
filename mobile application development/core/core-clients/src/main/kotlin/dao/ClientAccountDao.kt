package com.vt.dao

import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq

object ClientAccountDao {

    fun linkAccount(clntId: Int, accountId: Int) = transaction {
        try {
            ClientAccountTable.insert {
                it[ClientAccountTable.clntId] = clntId
                it[ClientAccountTable.accountId] = accountId
            }
        } catch (e: ExposedSQLException) {
            throw IllegalStateException("Account $accountId is already linked to a client")
        }
    }

    fun unlinkAccount(clntId: Int, accountId: Int): Boolean = transaction {
        ClientAccountTable.deleteWhere {
            (ClientAccountTable.clntId eq clntId) and
                    (ClientAccountTable.accountId eq accountId)
        } > 0
    }

    fun getAccounts(clntId: Int): List<Int> = transaction {
        ClientAccountTable.selectAll()
            .where { ClientAccountTable.clntId eq clntId }
            .map { it[ClientAccountTable.accountId] }
    }

    fun getClientIdByAccountId(accountId: Int): Int? = transaction {
        ClientAccountTable.selectAll()
            .where { ClientAccountTable.accountId eq accountId }
            .map { it[ClientAccountTable.clntId] }
            .singleOrNull()
    }
}
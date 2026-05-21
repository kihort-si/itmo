package com.vt.plugins

import com.vt.dao.*
import com.vt.model.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.configureRouting() {
    routing {
        route("/api/v1/clients") {

            // 1. POST /client - Create client
            post("/client") {
                val request = call.receive<CreateClientRequest>()
                val created = ClientDao.create(request)
                call.respond(HttpStatusCode.Created, created)
            }

            // 2. GET /client/{client_id} - Get client
            get("/client/{client_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                val client = ClientDao.findByClntId(clntId)
                    ?: throw NoSuchElementException("Client not found")
                call.respond(client)
            }

            // 3. PATCH /client/{client_id} - Update client_details
            patch("/client/{client_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val request = call.receive<UpdateClientDetailsRequest>()
                val updated = ClientDao.updateDetails(clntId, request)
                call.respond(updated)
            }

            // 4. PATCH /client/{client_id}/system_data - Update client system fields
            patch("/client/{client_id}/system_data") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val request = call.receive<UpdateSystemDataRequest>()
                val updated = ClientDao.updateSystemData(clntId, request)
                call.respond(updated)
            }

            // 5. POST /client/{client_id}/account - Link account
            post("/client/{client_id}/account") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val request = call.receive<LinkAccountRequest>()
                ClientAccountDao.linkAccount(clntId, request.accountId)
                call.respond(HttpStatusCode.Created)
            }

            // 6. DELETE /client/{client_id}/account/{account_id} - Unlink account
            delete("/client/{client_id}/account/{account_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                val accountId = call.parameters["account_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid account_id")
                val deleted = ClientAccountDao.unlinkAccount(clntId, accountId)
                if (!deleted) throw NoSuchElementException("Account link not found")
                call.respond(HttpStatusCode.NoContent)
            }

            // 7. GET /client/{client_id}/attribute - Get all attributes
            get("/client/{client_id}/attribute") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val attributes = ClientAttributeDao.findByClntId(clntId)
                call.respond(attributes)
            }

            // 8. GET /client/{client_id}/attribute/{attribute_id} - Get specific attribute
            get("/client/{client_id}/attribute/{attribute_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                val attributeRefsId = call.parameters["attribute_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid attribute_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val attribute = ClientAttributeDao.findByAttributeRefsId(clntId, attributeRefsId)
                    ?: throw NoSuchElementException("Attribute not found")
                call.respond(attribute)
            }

            // 9. POST /client/{client_id}/attribute - Create attribute
            post("/client/{client_id}/attribute") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val request = call.receive<CreateAttributeRequest>()
                val created = ClientAttributeDao.create(clntId, request)
                call.respond(HttpStatusCode.Created, created)
            }

            // 10. PATCH /client/{client_id}/attribute/{attribute_id} - Update attribute end_date
            patch("/client/{client_id}/attribute/{attribute_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                val attributeRefsId = call.parameters["attribute_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid attribute_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val request = call.receive<UpdateAttributeRequest>()
                val updated = ClientAttributeDao.updateEndDate(clntId, attributeRefsId, request.endDate)
                call.respond(updated)
            }

            // 11. DELETE /client/{client_id}/attribute/{attribute_id} - Delete attribute
            delete("/client/{client_id}/attribute/{attribute_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                val attributeRefsId = call.parameters["attribute_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid attribute_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val deleted = ClientAttributeDao.delete(clntId, attributeRefsId)
                if (!deleted) throw NoSuchElementException("Attribute not found")
                call.respond(HttpStatusCode.NoContent)
            }

            // 12. GET /client/{client_id}/account - Get all accounts
            get("/client/{client_id}/account") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                val accounts = ClientAccountDao.getAccounts(clntId).map { ClientAccountResponse(it) }
                call.respond(accounts)
            }

            // 13. GET /clientByAccountId/{account_id} - Get client by account
            get("/clientByAccountId/{account_id}") {
                val accountId = call.parameters["account_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid account_id")
                val clntId = ClientAccountDao.getClientIdByAccountId(accountId)
                    ?: throw NoSuchElementException("Account not found")
                call.respond(mapOf("client_id" to clntId))
            }

            delete("/client/{client_id}") {
                val clntId = call.parameters["client_id"]?.toIntOrNull()
                    ?: throw IllegalArgumentException("Invalid client_id")
                if (!ClientDao.exists(clntId)) throw NoSuchElementException("Client not found")
                ClientDao.delete(clntId)
                call.respond(HttpStatusCode.NoContent)
            }

            post("/checkUsernameInUse") {
                val request = call.receive<CheckUsernameRequest>()
                val inUse = ClientDao.isUsernameInUse(request.username)
                call.respond(mapOf("inUse" to inUse))
            }

            post("/checkEmailInUse") {
                val request = call.receive<CheckEmailRequest>()
                val inUse = ClientDao.isEmailInUse(request.email)
                call.respond(mapOf("inUse" to inUse))
            }
        }
    }
}
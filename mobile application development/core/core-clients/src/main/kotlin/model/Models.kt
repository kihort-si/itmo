package com.vt.model

import com.fasterxml.jackson.databind.JsonNode
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class ClientFullResponse(
    val clntId: Int,
    val username: String,
    val regionRefsIdentifier: Int,
    val createdAt: Instant,
    val languageCode: String,
    val status: String,                // <-- новое поле
    val fullName: String,
    val email: String,
    val phoneNumber: String?,
    val additionalInfo: String?,
    val profileExtension: JsonNode?
)

@Serializable
data class CreateClientRequest(
    val username: String,
    val regionRefsIdentifier: Int,
    val languageCode: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String? = null,
    val additionalInfo: String? = null,
    val profileExtension: JsonNode? = null
)

@Serializable
data class UpdateClientDetailsRequest(
    val fullName: String? = null,
    val email: String? = null,
    val phoneNumber: String? = null,
    val additionalInfo: String? = null,
    val profileExtension: JsonNode? = null
)

@Serializable
data class UpdateSystemDataRequest(
    val username: String? = null,
    val regionRefsIdentifier: Int? = null,
    val languageCode: String? = null,
    val status: String? = null       // <-- новое поле: “ACTIVE” или “CLOSED”
)

@Serializable
data class LinkAccountRequest(
    val accountId: Int
)

@Serializable
data class CreateAttributeRequest(
    val attributeId: Int,
    val value: String,
    val endDate: Instant? = null
)

@Serializable
data class UpdateAttributeRequest(
    val endDate: Instant
)

@Serializable
data class AttributeResponse(
    val attributeClientId: Int,
    val attributeId: Int,
    val startDate: Instant,
    val endDate: Instant?,
    val value: String
)

@Serializable
data class ClientAccountResponse(
    val accountId: Int
)

@Serializable
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)

@Serializable
data class CheckUsernameRequest(val username: String)

@Serializable
data class CheckEmailRequest(val email: String)
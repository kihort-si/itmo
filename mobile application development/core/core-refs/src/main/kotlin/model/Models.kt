package com.vt.model

import kotlinx.serialization.Serializable

@Serializable
data class Language(
    val langId: Int,
    val code: String,
    val name: String,
    val isDefault: Boolean
)

@Serializable
data class EntityListSchema(
    val eschemaId: Int,
    val code: String,
    val schema: String // JSON string
)

@Serializable
data class EntityList(
    val entlId: Int,
    val entityListId: Int,
    val schemaCode: String,
    val langId: Int,
    val data: String
)

@Serializable
data class EntitySingle(
    val entsId: Int,
    val code: String,
    val langId: Int,
    val data: String
)

// Request/Response wrappers
@Serializable
data class CreateSchemaRequest(
    val code: String,
    val schema: Map<String, Any> // will be serialized to JSON
)

@Serializable
data class UpdateSchemaRequest(
    val schema: Map<String, Any>
)

@Serializable
data class DataListResponse(
    val meta: DataListMeta,
    val values: List<DataItem>
)

@Serializable
data class DataListMeta(
    val schemaCode: String,
    val languageCode: String
)

@Serializable
data class DataItem(
    val entityListId: Int,
    val data: Map<String, Any>
)

@Serializable
data class SingleValueResponse(
    val meta: SingleValueMeta,
    val value: Map<String, Any>
)

@Serializable
data class SingleValueMeta(
    val code: String,
    val languageCode: String
)

@Serializable
data class ErrorResponse(
    val timestamp: String,
    val status: Int,
    val error: String,
    val message: String,
    val path: String
)
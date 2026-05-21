package com.vt.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import com.vt.dao.*
import com.vt.model.*
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue

fun resolveLang(languageCode: String?): Pair<Int, Language> {
    val lang = if (languageCode != null) {
        LanguageDao.findByCode(languageCode) ?: throw IllegalArgumentException("Language code $languageCode not found")
    } else {
        LanguageDao.findDefault() ?: throw IllegalStateException("No default language configured")
    }
    return lang.langId to lang
}

/**
 * Возвращает пару (langId, lang) для запрошенного языка.
 * Если для запрошенного языка нет данных (проверяется переданным предикатом),
 * и при этом был явно указан languageCode (не дефолтный), то выполняется fallback на дефолтный язык.
 */
private inline fun resolveLangWithFallback(
    languageCode: String?,
    noDataForRequestedLang: (langId: Int) -> Boolean
): Pair<Int, Language> {
    // Сначала определяем запрошенный язык
    val requestedLang = if (languageCode != null) {
        LanguageDao.findByCode(languageCode) ?: throw IllegalArgumentException("Language code $languageCode not found")
    } else {
        LanguageDao.findDefault() ?: throw IllegalStateException("No default language configured")
    }

    // Если для запрошенного языка есть данные или languageCode не был передан (значит изначально просили дефолтный) – возвращаем его
    if (languageCode == null || !noDataForRequestedLang(requestedLang.langId)) {
        return requestedLang.langId to requestedLang
    }

    // Иначе пробуем дефолтный язык
    val defaultLang = LanguageDao.findDefault() ?: throw IllegalStateException("No default language configured")
    return defaultLang.langId to defaultLang
}

fun Application.configureRouting() {
    val mapper = ObjectMapper().findAndRegisterModules()

    routing {
        route("/api/v1/refs") {
            // 1. GET /languages
            get("/languages") {
                val languages = LanguageDao.findAll()
                call.respond(languages)
            }

            // 2. POST /data_schema
            post("/data_schema") {
                val request = call.receive<CreateSchemaRequest>()
                if (EntityListSchemaDao.existsByCode(request.code)) {
                    throw IllegalStateException("Schema with code ${request.code} already exists")
                }
                val schemaJson = mapper.writeValueAsString(request.schema)
                val created = EntityListSchemaDao.create(request.code, schemaJson)
                // Преобразуем schema в объект для красивого ответа
                val schemaMap = mapper.readValue<Map<String, Any>>(created.schema)
                call.respond(HttpStatusCode.Created, mapOf(
                    "eschemaId" to created.eschemaId,
                    "code" to created.code,
                    "schema" to schemaMap
                ))
            }

            // 3. PUT /data_schema/{code}
            put("/data_schema/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Missing code")
                val request = call.receive<UpdateSchemaRequest>()
                if (!EntityListSchemaDao.existsByCode(code)) {
                    throw NoSuchElementException("Schema with code $code not found")
                }
                val schemaJson = mapper.writeValueAsString(request.schema)
                val updated = EntityListSchemaDao.update(code, schemaJson)
                val schemaMap = mapper.readValue<Map<String, Any>>(updated.schema)
                call.respond(mapOf(
                    "eschemaId" to updated.eschemaId,
                    "code" to updated.code,
                    "schema" to schemaMap
                ))
            }

            // 4. GET /data_schema/{code}
            get("/data_schema/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Missing code")
                val schemaEntity = EntityListSchemaDao.findByCode(code) ?: throw NoSuchElementException("Schema not found")
                val schemaMap = mapper.readValue<Map<String, Any>>(schemaEntity.schema)
                call.respond(mapOf(
                    "eschemaId" to schemaEntity.eschemaId,
                    "code" to schemaEntity.code,
                    "schema" to schemaMap
                ))
            }

            // Helper: resolve langId from languageCode (optional)
            fun resolveLangId(languageCode: String?): Int {
                return if (languageCode != null) {
                    LanguageDao.findByCode(languageCode)?.langId
                        ?: throw IllegalArgumentException("Language code $languageCode not found")
                } else {
                    LanguageDao.findDefault()?.langId
                        ?: throw IllegalStateException("No default language configured")
                }
            }

            // 5. POST /data/{schema_code}
            post("/data/{schema_code}") {
                val schemaCode = call.parameters["schema_code"] ?: throw IllegalArgumentException("Missing schema_code")
                val languageCode = call.request.queryParameters["languageCode"]
                val langId = resolveLangId(languageCode)
                val requestData = call.receive<Map<String, Any>>()
                val entityListId = (requestData["entity_list_id"] as? Number)?.toInt()
                // Убираем служебное поле entity_list_id из данных
                val dataWithoutId = requestData.filterKeys { it != "entity_list_id" }
                val dataJson = mapper.writeValueAsString(dataWithoutId)
                val created = EntityListDao.create(schemaCode, entityListId, langId, dataJson)
                call.respond(mapOf("entity_list_id" to created.entityListId))
            }

            // 6. PUT /data/{schema_code}/{entity_list_id}
            put("/data/{schema_code}/{entity_list_id}") {
                val schemaCode = call.parameters["schema_code"] ?: throw IllegalArgumentException("Missing schema_code")
                val entityListId = call.parameters["entity_list_id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid entity_list_id")
                val languageCode = call.request.queryParameters["languageCode"]
                val langId = resolveLangId(languageCode)
                val requestData = call.receive<Map<String, Any>>()
                // Убираем entity_list_id, если вдруг передали
                val dataWithoutId = requestData.filterKeys { it != "entity_list_id" }
                val dataJson = mapper.writeValueAsString(dataWithoutId)
                val updated = EntityListDao.update(schemaCode, entityListId, langId, dataJson)
                val responseData = mapper.readValue<Map<String, Any>>(updated.data)
                call.respond(mapOf(
                    "entlId" to updated.entlId,
                    "entityListId" to updated.entityListId,
                    "schemaCode" to updated.schemaCode,
                    "langId" to updated.langId,
                    "data" to responseData
                ))
            }

            // 7. GET /data/{schema_code}
            get("/data/{schema_code}") {
                val schemaCode = call.parameters["schema_code"] ?: throw IllegalArgumentException("Missing schema_code")
                val languageCode = call.request.queryParameters["languageCode"]

                val (langId, lang) = resolveLangWithFallback(languageCode) { langId ->
                    EntityListDao.findBySchemaAndLang(schemaCode, langId).isEmpty()
                }

                val items = EntityListDao.findBySchemaAndLang(schemaCode, langId)
                val response = DataListResponse(
                    meta = DataListMeta(schemaCode, lang.code),
                    values = items.map { DataItem(it.entityListId, mapper.readValue(it.data)) }
                )
                call.respond(response)
            }

            // 8. GET /data/{schema_code}/{entity_list_id}
            get("/data/{schema_code}/{entity_list_id}") {
                val schemaCode = call.parameters["schema_code"] ?: throw IllegalArgumentException("Missing schema_code")
                val entityListId = call.parameters["entity_list_id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid entity_list_id")
                val languageCode = call.request.queryParameters["languageCode"]

                val (langId, lang) = resolveLangWithFallback(languageCode) { langId ->
                    EntityListDao.findBySchemaLangAndId(schemaCode, entityListId, langId) == null
                }

                val item = EntityListDao.findBySchemaLangAndId(schemaCode, entityListId, langId)
                    ?: throw NoSuchElementException("Data not found even for default language")

                val response = DataItem(item.entityListId, mapper.readValue(item.data))
                call.respond(response)
            }

            // 9. GET /refval/{code}
            get("/refval/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Missing code")
                val languageCode = call.request.queryParameters["languageCode"]

                val (langId, lang) = resolveLangWithFallback(languageCode) { langId ->
                    EntitySingleDao.findByCodeAndLang(code, langId) == null
                }

                val entity = EntitySingleDao.findByCodeAndLang(code, langId)
                    ?: throw NoSuchElementException("Refval not found for code $code even for default language")

                val response = SingleValueResponse(
                    meta = SingleValueMeta(code, lang.code),
                    value = mapper.readValue(entity.data)
                )
                call.respond(response)
            }

            // 10. POST /refval
            post("/refval") {
                val languageCode = call.request.queryParameters["languageCode"]
                val langId = resolveLangId(languageCode)
                val requestData = call.receive<Map<String, Any>>()
                val code = requestData["code"] as? String ?: throw IllegalArgumentException("Missing 'code' field")
                // Убираем code из данных
                val dataWithoutCode = requestData.filterKeys { it != "code" }
                val dataJson = mapper.writeValueAsString(dataWithoutCode)
                val created = EntitySingleDao.create(code, langId, dataJson)
                val responseData = mapper.readValue<Map<String, Any>>(created.data)
                call.respond(HttpStatusCode.Created, mapOf(
                    "entsId" to created.entsId,
                    "code" to created.code,
                    "langId" to created.langId,
                    "data" to responseData
                ))
            }

            // 11. PUT /refval/{code}
            put("/refval/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Missing code")
                val languageCode = call.request.queryParameters["languageCode"]
                val langId = resolveLangId(languageCode)
                val requestData = call.receive<Map<String, Any>>()
                // Убираем code, если он вдруг передан
                val dataWithoutCode = requestData.filterKeys { it != "code" }
                val dataJson = mapper.writeValueAsString(dataWithoutCode)
                val updated = EntitySingleDao.update(code, langId, dataJson)
                val responseData = mapper.readValue<Map<String, Any>>(updated.data)
                call.respond(mapOf(
                    "entsId" to updated.entsId,
                    "code" to updated.code,
                    "langId" to updated.langId,
                    "data" to responseData
                ))
            }

            // 12. DELETE /data/{schema_code}/{entity_list_id}
            delete("/data/{schema_code}/{entity_list_id}") {
                val schemaCode = call.parameters["schema_code"] ?: throw IllegalArgumentException("Missing schema_code")
                val entityListId = call.parameters["entity_list_id"]?.toIntOrNull() ?: throw IllegalArgumentException("Invalid entity_list_id")
                val languageCode = call.request.queryParameters["languageCode"] ?: throw IllegalArgumentException("languageCode is required for DELETE")
                val langId = resolveLangId(languageCode)
                val deleted = EntityListDao.delete(schemaCode, entityListId, langId)
                if (!deleted) throw NoSuchElementException("Data not found")
                call.respond(HttpStatusCode.NoContent)
            }

            // 13. DELETE /refval/{code}
            delete("/refval/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Missing code")
                val languageCode = call.request.queryParameters["languageCode"] ?: throw IllegalArgumentException("languageCode is required for DELETE")
                val langId = resolveLangId(languageCode)
                val deleted = EntitySingleDao.delete(code, langId)
                if (!deleted) throw NoSuchElementException("Refval not found")
                call.respond(HttpStatusCode.NoContent)
            }

            // 14. DELETE /data_schema/{code}
            delete("/data_schema/{code}") {
                val code = call.parameters["code"] ?: throw IllegalArgumentException("Missing code")
                val deleted = EntityListSchemaDao.deleteByCode(code)
                if (!deleted) throw NoSuchElementException("Schema not found")
                call.respond(HttpStatusCode.NoContent)
            }
        }
    }
}
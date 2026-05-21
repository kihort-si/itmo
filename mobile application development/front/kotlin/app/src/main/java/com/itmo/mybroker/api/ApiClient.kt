package com.itmo.mybroker.api

import android.util.Log
import com.google.gson.Gson
import com.itmo.mybroker.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object ApiClient {
    private const val TAG = "MyBrokerApi"

    @PublishedApi
    internal val gson = Gson()

    @PublishedApi
    internal val jsonMedia = "application/json; charset=utf-8".toMediaType()

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    @Volatile
    var tokenProvider: () -> String? = { null }

    fun gson(): Gson = gson

    inline fun <reified T> request(
        path: String,
        method: String = "GET",
        body: Any? = null,
        auth: Boolean = true,
    ): T {
        if (BuildConfig.USE_MOCK_API) {
            logDebug("MOCK MODE blocks HTTP request: $method $path")
            throw ApiException("Mock API mode", 0)
        }

        val url = BuildConfig.API_BASE_URL.trimEnd('/') + path
        val reqBuilder = Request.Builder().url(url)
        val startedAt = System.currentTimeMillis()
        var requestJson: String? = null

        if (body != null) {
            val json = gson.toJson(body)
            requestJson = json
            reqBuilder.method(method, json.toRequestBody(jsonMedia))
        } else {
            reqBuilder.method(method, if (method == "GET" || method == "HEAD") null else "".toRequestBody(jsonMedia))
        }

        reqBuilder.header("Accept", "application/json")
        val token = tokenProvider()
        if (auth) {
            token?.let { reqBuilder.header("Authorization", "Bearer $it") }
        }

        logDebug(
            buildString {
                append("REQUEST ")
                append(method)
                append(" ")
                append(url)
                append(" auth=")
                append(auth)
                append(" tokenPresent=")
                append(!token.isNullOrBlank())
                append(" mock=")
                append(BuildConfig.USE_MOCK_API)
                requestJson?.let {
                    append(" body=")
                    append(redact(it))
                }
            },
        )

        val res = try {
            client.newCall(reqBuilder.build()).execute()
        } catch (e: Exception) {
            logError("NETWORK ERROR $method $url after ${System.currentTimeMillis() - startedAt}ms: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
        val raw = res.body?.string()
        logDebug(
            "RESPONSE $method $url status=${res.code} elapsedMs=${System.currentTimeMillis() - startedAt} body=${redact(raw.orEmpty())}",
        )

        if (!res.isSuccessful) {
            throw ApiException("HTTP ${res.code}", res.code, raw)
        }

        if (res.code == 204 || raw.isNullOrBlank()) {
            @Suppress("UNCHECKED_CAST")
            return null as T
        }

        return gson.fromJson(raw, T::class.java)
    }

    fun postEmpty(path: String, auth: Boolean = true) {
        if (BuildConfig.USE_MOCK_API) {
            logDebug("MOCK MODE blocks HTTP request: POST $path")
            throw ApiException("Mock API mode", 0)
        }
        val url = BuildConfig.API_BASE_URL.trimEnd('/') + path
        val startedAt = System.currentTimeMillis()
        val reqBuilder = Request.Builder()
            .url(url)
            .post("".toRequestBody(jsonMedia))
            .header("Accept", "application/json")
        val token = tokenProvider()
        if (auth) {
            token?.let { reqBuilder.header("Authorization", "Bearer $it") }
        }
        logDebug("REQUEST POST $url auth=$auth tokenPresent=${!token.isNullOrBlank()} body=<empty>")
        val res = try {
            client.newCall(reqBuilder.build()).execute()
        } catch (e: Exception) {
            logError("NETWORK ERROR POST $url after ${System.currentTimeMillis() - startedAt}ms: ${e.javaClass.simpleName}: ${e.message}", e)
            throw e
        }
        val raw = res.body?.string()
        logDebug("RESPONSE POST $url status=${res.code} elapsedMs=${System.currentTimeMillis() - startedAt} body=${redact(raw.orEmpty())}")
        if (!res.isSuccessful) {
            throw ApiException("HTTP ${res.code}", res.code, raw)
        }
        res.close()
    }

    @PublishedApi
    internal fun logDebug(message: String) {
        Log.d(TAG, message)
    }

    @PublishedApi
    internal fun logError(message: String, error: Throwable? = null) {
        if (error == null) Log.e(TAG, message) else Log.e(TAG, message, error)
    }

    @PublishedApi
    internal fun redact(raw: String): String {
        if (raw.isBlank()) return "<empty>"
        return raw
            .replace(Regex(""""password"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE), """"password":"***"""")
            .replace(Regex(""""accessToken"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE), """"accessToken":"***"""")
            .replace(Regex(""""refreshToken"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE), """"refreshToken":"***"""")
            .replace(Regex(""""token"\s*:\s*"[^"]*"""", RegexOption.IGNORE_CASE), """"token":"***"""")
            .take(4000)
    }
}

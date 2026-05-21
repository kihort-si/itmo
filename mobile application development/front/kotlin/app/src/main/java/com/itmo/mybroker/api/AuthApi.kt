package com.itmo.mybroker.api

object AuthApi {
    private const val PREFIX = "/api/broker-app/v1"

    fun register(body: RegisterRequest): AuthResponse =
        ApiClient.request("$PREFIX/auth/register", method = "POST", body = body, auth = false)

    fun login(body: LoginRequest): AuthResponse =
        ApiClient.request("$PREFIX/auth/login", method = "POST", body = body, auth = false)

    fun logout() {
        ApiClient.postEmpty("$PREFIX/auth/logout")
    }

    fun checkUsernameInUse(username: String): AvailabilityResponse =
        ApiClient.request(
            "$PREFIX/auth/checkUsernameInUse",
            method = "POST",
            body = mapOf("username" to username),
            auth = false,
        )

    fun checkEmailInUse(email: String): AvailabilityResponse =
        ApiClient.request(
            "$PREFIX/auth/checkEmailInUse",
            method = "POST",
            body = mapOf("email" to email),
            auth = false,
        )
}

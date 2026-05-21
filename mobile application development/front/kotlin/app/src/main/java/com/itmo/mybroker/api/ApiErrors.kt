package com.itmo.mybroker.api

class ApiException(
    message: String,
    val status: Int,
    val body: String? = null,
) : Exception(message)

fun isNetworkOrMockError(e: Throwable): Boolean =
    e is ApiException && e.status == 0 ||
        e is java.net.SocketTimeoutException ||
        e is java.net.UnknownHostException ||
        e is java.io.IOException

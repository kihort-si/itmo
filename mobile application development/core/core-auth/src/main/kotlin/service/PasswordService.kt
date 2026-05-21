package com.vt.service

import java.security.SecureRandom
import java.util.Base64
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object PasswordService {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private val random = SecureRandom()

    fun hash(password: String): String {
        require(password.length >= 8) { "Password must be at least 8 characters long" }
        val salt = ByteArray(16)
        random.nextBytes(salt)
        val hash = derive(password, salt)
        return listOf(
            ALGORITHM,
            ITERATIONS.toString(),
            Base64.getEncoder().encodeToString(salt),
            Base64.getEncoder().encodeToString(hash)
        ).joinToString("$")
    }

    fun verify(password: String, encoded: String): Boolean {
        val parts = encoded.split("$")
        if (parts.size != 4) return false

        val algorithm = parts[0]
        val iterations = parts[1].toIntOrNull() ?: return false
        val salt = Base64.getDecoder().decode(parts[2])
        val expected = Base64.getDecoder().decode(parts[3])
        val actual = derive(password, salt, iterations, algorithm)
        return actual.contentEquals(expected)
    }

    private fun derive(
        password: String,
        salt: ByteArray,
        iterations: Int = ITERATIONS,
        algorithm: String = ALGORITHM
    ): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, iterations, KEY_LENGTH)
        return SecretKeyFactory.getInstance(algorithm).generateSecret(spec).encoded
    }
}

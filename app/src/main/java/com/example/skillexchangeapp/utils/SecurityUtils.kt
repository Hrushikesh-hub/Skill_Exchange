package com.example.skillexchangeapp.utils

import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

object SecurityUtils {
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH = 256
    private val demoSalt = "SkillExchangeUltimateLocalSalt".toByteArray()

    fun hashPassword(password: String): String {
        val spec = PBEKeySpec(password.toCharArray(), demoSalt, ITERATIONS, KEY_LENGTH)
        val bytes = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun newToken(): String {
        val bytes = ByteArray(24)
        SecureRandom().nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }
}

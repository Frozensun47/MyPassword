package com.myapplications.mypasswords.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class SecurityManager(context: Context) {

    companion object {
        private const val PREFS_NAME = "secure_password_prefs"
        private const val KEY_USER_PIN = "user_pin_hash"
        private const val KEY_FAILED_ATTEMPTS = "failed_attempts"
        private const val KEY_LOCKOUT_TIMESTAMP = "lockout_timestamp"
        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MINUTES = 5
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val sharedPreferences: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // --- PIN Management ---

    fun isPinSet(): Boolean {
        return sharedPreferences.contains(KEY_USER_PIN)
    }

    fun validatePinFormat(pin: String): Boolean {
        return pin.length in 4..8 && pin.all { it.isDigit() }
    }

    fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        sharedPreferences.edit().putString(KEY_USER_PIN, hashedPin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedPinHash = sharedPreferences.getString(KEY_USER_PIN, null)
        val isCorrect = storedPinHash == hashPin(pin)
        if (isCorrect) {
            resetFailedAttempts()
        } else {
            recordFailedAttempt()
        }
        return isCorrect
    }

    // --- Lockout Logic ---

    fun getLockoutTimestamp(): Long {
        return sharedPreferences.getLong(KEY_LOCKOUT_TIMESTAMP, 0L)
    }

    fun isLockedOut(): Boolean {
        val lockoutTime = getLockoutTimestamp()
        return System.currentTimeMillis() < lockoutTime
    }

    private fun recordFailedAttempt() {
        val currentAttempts = sharedPreferences.getInt(KEY_FAILED_ATTEMPTS, 0) + 1
        if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
            val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MINUTES * 60 * 1000
            sharedPreferences.edit()
                .putLong(KEY_LOCKOUT_TIMESTAMP, lockoutUntil)
                .putInt(KEY_FAILED_ATTEMPTS, 0) // Reset after setting lockout
                .apply()
        } else {
            sharedPreferences.edit().putInt(KEY_FAILED_ATTEMPTS, currentAttempts).apply()
        }
    }

    private fun resetFailedAttempts() {
        sharedPreferences.edit()
            .putInt(KEY_FAILED_ATTEMPTS, 0)
            .remove(KEY_LOCKOUT_TIMESTAMP) // Also clear any old lockout
            .apply()
    }


    // --- Hashing ---

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun savePassword(password: com.myapplications.mypasswords.model.Password) {
        val allPasswords = getPasswords().toMutableList()
        val existingPassword = allPasswords.find { it.id == password.id }
        if (existingPassword != null) {
            allPasswords.remove(existingPassword)
        }
        allPasswords.add(password)
        sharedPreferences.edit().putString("passwords", allPasswords.joinToString(";;;") { "${it.id},${it.title},${it.username},${it.password}" }).apply()
    }

    fun getPasswords(): List<com.myapplications.mypasswords.model.Password> {
        val passwordsString = sharedPreferences.getString("passwords", null) ?: return emptyList()
        return passwordsString.split(";;;").mapNotNull {
            val parts = it.split(",")
            if (parts.size == 4) {
                com.myapplications.mypasswords.model.Password(parts[0], parts[1], parts[2], parts[3])
            } else {
                null
            }
        }
    }

    fun deletePassword(password: com.myapplications.mypasswords.model.Password) {
        val allPasswords = getPasswords().toMutableList()
        allPasswords.removeAll { it.id == password.id }
        sharedPreferences.edit().putString("passwords", allPasswords.joinToString(";;;") { "${it.id},${it.title},${it.username},${it.password}" }).apply()
    }
    fun deleteAllData() {
        sharedPreferences.edit().clear().apply()
    }
}
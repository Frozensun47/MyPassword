// FILE: com/myapplications/mypasswords/security/SecurityManager.kt
package com.myapplications.mypasswords.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import kotlinx.coroutines.flow.first
import java.security.KeyStore
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Define DataStore instance at the top level
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "security_prefs")

class SecurityManager(private val context: Context) {

    private object PrefKeys {
        val USER_PIN_HASH = stringPreferencesKey("user_pin_hash")
        val FAILED_ATTEMPTS = intPreferencesKey("failed_pin_attempts")
        val LOCKOUT_TIMESTAMP = longPreferencesKey("lockout_timestamp")
    }

    companion object {
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30 * 1000L // 30 seconds
    }

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    fun getDatabasePassphrase(): String {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "db_secure_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        val keyDbPassphrase = "db_passphrase"
        val existingPassphrase = sharedPreferences.getString(keyDbPassphrase, null)

        if (existingPassphrase != null) {
            return existingPassphrase
        }

        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val newPassphrase = Base64.encodeToString(bytes, Base64.NO_WRAP)
        sharedPreferences.edit().putString(keyDbPassphrase, newPassphrase).apply()
        return newPassphrase
    }


    private fun getOrCreateSecretKey(alias: String): SecretKey {
        return (keyStore.getKey(alias, null) as? SecretKey) ?: generateSecretKey(alias)
    }

    private fun generateSecretKey(alias: String): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        val parameterSpec = KeyGenParameterSpec.Builder(
            alias,
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        ).apply {
            setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            setKeySize(256)
        }.build()
        keyGenerator.init(parameterSpec)
        return keyGenerator.generateKey()
    }

    fun encrypt(data: String, alias: String = "password_key"): String {
        val secretKey = getOrCreateSecretKey(alias)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val iv = cipher.iv
        val encryptedData = cipher.doFinal(data.toByteArray())
        val combined = iv + encryptedData
        return Base64.encodeToString(combined, Base64.DEFAULT)
    }

    fun decrypt(encryptedString: String, alias: String = "password_key"): String {
        try {
            val combined = Base64.decode(encryptedString, Base64.DEFAULT)
            val secretKey = getOrCreateSecretKey(alias)
            val iv = combined.copyOfRange(0, 12)
            val encryptedData = combined.copyOfRange(12, combined.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val spec = GCMParameterSpec(128, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)
            return String(cipher.doFinal(encryptedData))
        } catch (e: Exception) {
            e.printStackTrace()
            return ""
        }
    }

    // --- Corrected PIN Management ---

    private suspend fun readPreferences() = context.dataStore.data.first()

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    suspend fun isPinSet(): Boolean {
        return readPreferences().contains(PrefKeys.USER_PIN_HASH)
    }

    fun validatePinFormat(pin: String): Boolean {
        // The ViewModel now enforces a 6-digit PIN
        return pin.length == 6 && pin.all { it.isDigit() }
    }

    suspend fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        context.dataStore.edit { settings ->
            settings[PrefKeys.USER_PIN_HASH] = hashedPin
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
        if (isLockedOut()) return false

        val preferences = readPreferences()
        val storedPinHash = preferences[PrefKeys.USER_PIN_HASH]
        val isCorrect = storedPinHash == hashPin(pin)

        if (isCorrect) {
            resetFailedAttempts()
        } else {
            recordFailedAttempt()
        }
        return isCorrect
    }

    private suspend fun recordFailedAttempt() {
        context.dataStore.edit { settings ->
            val currentAttempts = settings[PrefKeys.FAILED_ATTEMPTS] ?: 0
            val newAttempts = currentAttempts + 1
            settings[PrefKeys.FAILED_ATTEMPTS] = newAttempts
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                settings[PrefKeys.LOCKOUT_TIMESTAMP] = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            }
        }
    }

    private suspend fun resetFailedAttempts() {
        context.dataStore.edit { settings ->
            settings.remove(PrefKeys.FAILED_ATTEMPTS)
            settings.remove(PrefKeys.LOCKOUT_TIMESTAMP)
        }
    }

    suspend fun isLockedOut(): Boolean {
        val lockoutTime = readPreferences()[PrefKeys.LOCKOUT_TIMESTAMP] ?: 0L
        return System.currentTimeMillis() < lockoutTime
    }

    suspend fun getLockoutTimestamp(): Long {
        return readPreferences()[PrefKeys.LOCKOUT_TIMESTAMP] ?: 0L
    }
}

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
// Removed deprecated imports
// import androidx.security.crypto.EncryptedSharedPreferences
// import androidx.security.crypto.MasterKey
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

// No longer accepts a Context in the constructor
class SecurityManager {

    private object PrefKeys {
        val USER_PIN_HASH = stringPreferencesKey("user_pin_hash")
        val FAILED_ATTEMPTS = intPreferencesKey("failed_pin_attempts")
        val LOCKOUT_TIMESTAMP = longPreferencesKey("lockout_timestamp")
        // New key for the encrypted database passphrase
        val ENCRYPTED_DB_PASSPHRASE = stringPreferencesKey("encrypted_db_passphrase")
    }

    companion object {
        private const val MAX_FAILED_ATTEMPTS = 5
        private const val LOCKOUT_DURATION_MS = 30 * 1000L // 30 seconds
        private const val DB_PASSPHRASE_ALIAS = "db_passphrase_key" // Alias for the key used to encrypt the DB passphrase
    }

    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Retrieves or generates a secure passphrase for the database.
     * This method now uses DataStore to store the encrypted passphrase and
     * leverages the existing AES/GCM encryption/decryption logic within this class,
     * replacing the deprecated EncryptedSharedPreferences.
     *
     * @param context The application context.
     * @return The decrypted database passphrase.
     */
    suspend fun getDatabasePassphrase(context: Context): String {
        val preferences = readPreferences(context)
        val encryptedPassphrase = preferences[PrefKeys.ENCRYPTED_DB_PASSPHRASE]

        if (encryptedPassphrase != null) {
            // If an encrypted passphrase exists, decrypt and return it
            return decrypt(encryptedPassphrase, DB_PASSPHRASE_ALIAS)
        }

        // If no passphrase exists, generate a new one
        val random = SecureRandom()
        val bytes = ByteArray(32) // 32 bytes for a strong passphrase
        random.nextBytes(bytes)
        val newPassphrase = Base64.encodeToString(bytes, Base64.NO_WRAP)

        // Encrypt the new passphrase using the existing encryption logic
        val encryptedNewPassphrase = encrypt(newPassphrase, DB_PASSPHRASE_ALIAS)

        // Store the encrypted passphrase in DataStore
        context.dataStore.edit { settings ->
            settings[PrefKeys.ENCRYPTED_DB_PASSPHRASE] = encryptedNewPassphrase
        }

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

    // Now requires a Context parameter
    private suspend fun readPreferences(context: Context) = context.dataStore.data.first()

    private fun hashPin(pin: String): String {
        val bytes = pin.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return Base64.encodeToString(digest, Base64.NO_WRAP)
    }

    // Now requires a Context parameter
    suspend fun isPinSet(context: Context): Boolean {
        return readPreferences(context).contains(PrefKeys.USER_PIN_HASH)
    }

    // Now requires a Context parameter
    suspend fun savePin(context: Context, pin: String) {
        val hashedPin = hashPin(pin)
        context.dataStore.edit { settings ->
            settings[PrefKeys.USER_PIN_HASH] = hashedPin
        }
    }

    // Now requires a Context parameter
    suspend fun verifyPin(context: Context, pin: String): Boolean {
        if (isLockedOut(context)) return false

        val preferences = readPreferences(context)
        val storedPinHash = preferences[PrefKeys.USER_PIN_HASH]
        val isCorrect = storedPinHash == hashPin(pin)

        if (isCorrect) {
            resetFailedAttempts(context)
        } else {
            recordFailedAttempt(context)
        }
        return isCorrect
    }

    // Now requires a Context parameter
    private suspend fun recordFailedAttempt(context: Context) {
        context.dataStore.edit { settings ->
            val currentAttempts = settings[PrefKeys.FAILED_ATTEMPTS] ?: 0
            val newAttempts = currentAttempts + 1
            settings[PrefKeys.FAILED_ATTEMPTS] = newAttempts
            if (newAttempts >= MAX_FAILED_ATTEMPTS) {
                settings[PrefKeys.LOCKOUT_TIMESTAMP] = System.currentTimeMillis() + LOCKOUT_DURATION_MS
            }
        }
    }

    // Now requires a Context parameter
    private suspend fun resetFailedAttempts(context: Context) {
        context.dataStore.edit { settings ->
            settings.remove(PrefKeys.FAILED_ATTEMPTS)
            settings.remove(PrefKeys.LOCKOUT_TIMESTAMP)
        }
    }

    // Now requires a Context parameter
    suspend fun isLockedOut(context: Context): Boolean {
        val lockoutTime = readPreferences(context)[PrefKeys.LOCKOUT_TIMESTAMP] ?: 0L
        return System.currentTimeMillis() < lockoutTime
    }

    // Now requires a Context parameter
    suspend fun getLockoutTimestamp(context: Context): Long {
        return readPreferences(context)[PrefKeys.LOCKOUT_TIMESTAMP] ?: 0L
    }
}
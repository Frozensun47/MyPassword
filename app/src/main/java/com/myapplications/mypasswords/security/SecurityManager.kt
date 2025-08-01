package com.myapplications.mypasswords.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

// Define the DataStore instance at the top level of the file
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "secure_password_prefs")

class SecurityManager(private val context: Context) {

    // Define keys for DataStore for type safety
    private object PrefKeys {
        val USER_PIN_HASH = stringPreferencesKey("user_pin_hash")
        val FAILED_ATTEMPTS = intPreferencesKey("failed_attempts")
        val LOCKOUT_TIMESTAMP = longPreferencesKey("lockout_timestamp")
        val ENCRYPTED_DB_PASSPHRASE = stringPreferencesKey("encrypted_db_passphrase")
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEYSTORE_ALIAS_DATABASE = "db_passphrase_key"
        private const val ENCRYPTION_TRANSFORMATION = "AES/GCM/NoPadding"
        private const val ENCRYPTION_IV_SIZE_BYTES = 12 // GCM recommended IV size is 12 bytes

        const val MAX_FAILED_ATTEMPTS = 5
        const val LOCKOUT_DURATION_MINUTES = 5
    }

    // --- PIN Management ---

    suspend fun isPinSet(): Boolean {
        return readPreferences().contains(PrefKeys.USER_PIN_HASH)
    }

    fun validatePinFormat(pin: String): Boolean {
        return pin.length in 4..8 && pin.all { it.isDigit() }
    }

    suspend fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        context.dataStore.edit { settings ->
            settings[PrefKeys.USER_PIN_HASH] = hashedPin
        }
    }

    suspend fun verifyPin(pin: String): Boolean {
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

    // --- Database Passphrase Management (using Android Keystore) ---

    suspend fun getDatabasePassphrase(): String {
        val preferences = readPreferences()
        val encryptedPassphraseHex = preferences[PrefKeys.ENCRYPTED_DB_PASSPHRASE]

        return if (encryptedPassphraseHex != null) {
            // If passphrase exists, decrypt and return it
            val key = getKeystoreKey(KEYSTORE_ALIAS_DATABASE)
            decrypt(encryptedPassphraseHex, key)
        } else {
            // Otherwise, generate a new one, encrypt it, store it, and return it
            val newPassphrase = generateRandomPassphrase()
            val key = getKeystoreKey(KEYSTORE_ALIAS_DATABASE)
            val encrypted = encrypt(newPassphrase, key)
            context.dataStore.edit { settings ->
                settings[PrefKeys.ENCRYPTED_DB_PASSPHRASE] = encrypted
            }
            newPassphrase
        }
    }

    // --- Lockout Logic ---

    suspend fun isLockedOut(): Boolean {
        val lockoutTime = readPreferences()[PrefKeys.LOCKOUT_TIMESTAMP] ?: 0L
        return System.currentTimeMillis() < lockoutTime
    }

    /**
     * Retrieves the timestamp (in milliseconds) when the current lockout will end.
     * Returns 0L if the app is not currently locked out.
     */
    suspend fun getLockoutTimestamp(): Long {
        return readPreferences()[PrefKeys.LOCKOUT_TIMESTAMP] ?: 0L
    }

    private suspend fun recordFailedAttempt() {
        context.dataStore.edit { settings ->
            val currentAttempts = (settings[PrefKeys.FAILED_ATTEMPTS] ?: 0) + 1
            if (currentAttempts >= MAX_FAILED_ATTEMPTS) {
                val lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MINUTES * 60 * 1000
                settings[PrefKeys.LOCKOUT_TIMESTAMP] = lockoutUntil
                settings[PrefKeys.FAILED_ATTEMPTS] = 0 // Reset after lockout is set
            } else {
                settings[PrefKeys.FAILED_ATTEMPTS] = currentAttempts
            }
        }
    }

    private suspend fun resetFailedAttempts() {
        context.dataStore.edit { settings ->
            settings.remove(PrefKeys.FAILED_ATTEMPTS)
            settings.remove(PrefKeys.LOCKOUT_TIMESTAMP)
        }
    }

    suspend fun deleteAllData() {
        context.dataStore.edit { it.clear() }
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.deleteEntry(KEYSTORE_ALIAS_DATABASE)
    }

    // --- Helpers and Cryptography ---

    private suspend fun readPreferences(): Preferences {
        return context.dataStore.data.catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }.first()
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(pin.toByteArray(StandardCharsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun generateRandomPassphrase(length: Int = 32): String {
        val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
        return (1..length).map { allowedChars.random() }.joinToString("")
    }

    private fun getKeystoreKey(alias: String): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        keyStore.getKey(alias, null)?.let { return it as SecretKey }

        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setKeySize(256)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .build()

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).apply {
            init(spec)
        }.generateKey()
    }

    private fun encrypt(data: String, key: SecretKey): String {
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, key)
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data.toByteArray(StandardCharsets.UTF_8))

        val byteBuffer = ByteBuffer.allocate(iv.size + encryptedBytes.size)
        byteBuffer.put(iv)
        byteBuffer.put(encryptedBytes)
        return bytesToHex(byteBuffer.array())
    }

    private fun decrypt(encryptedDataHex: String, key: SecretKey): String {
        val encryptedDataBytes = hexToBytes(encryptedDataHex)
        val spec = GCMParameterSpec(128, encryptedDataBytes, 0, ENCRYPTION_IV_SIZE_BYTES)
        val cipher = Cipher.getInstance(ENCRYPTION_TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, spec)

        val encryptedContentOffset = ENCRYPTION_IV_SIZE_BYTES
        val encryptedContentLength = encryptedDataBytes.size - ENCRYPTION_IV_SIZE_BYTES
        val decryptedBytes = cipher.doFinal(encryptedDataBytes, encryptedContentOffset, encryptedContentLength)
        return String(decryptedBytes, StandardCharsets.UTF_8)
    }

    private fun bytesToHex(bytes: ByteArray): String = bytes.joinToString("") { "%02x".format(it) }
    private fun hexToBytes(hex: String): ByteArray = hex.chunked(2).map { it.toInt(16).toByte() }.toByteArray()
}

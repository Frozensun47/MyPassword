package com.myapplications.mypasswords.security
import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.security.MessageDigest

class SecurityManager(context: Context) {

    private val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

    private val sharedPreferences = EncryptedSharedPreferences.create(
            context,
            "password_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePin(pin: String) {
        val hashedPin = hashPin(pin)
        sharedPreferences.edit().putString("user_pin", hashedPin).apply()
    }

    fun verifyPin(pin: String): Boolean {
        val storedPin = sharedPreferences.getString("user_pin", null)
        return storedPin == hashPin(pin)
    }

    fun isPinSet(): Boolean {
        return sharedPreferences.contains("user_pin")
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashedBytes = digest.digest(pin.toByteArray(Charsets.UTF_8))
        return bytesToHex(hashedBytes)
    }

    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789abcdef"
        val result = StringBuilder(bytes.size * 2)
        for (byte in bytes) {
            val i = byte.toInt()
            result.append(hexChars[i shr 4 and 0x0f])
            result.append(hexChars[i and 0x0f])
        }
        return result.toString()
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
}
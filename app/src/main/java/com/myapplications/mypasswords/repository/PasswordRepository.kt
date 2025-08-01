package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.database.DatabaseProvider
import com.myapplications.mypasswords.database.PasswordDao
import com.myapplications.mypasswords.model.Password
import kotlinx.coroutines.flow.Flow

/**
 * Singleton repository for managing passwords.
 * Requires initialization before use.
 */
object PasswordRepository {

    private lateinit var passwordDao: PasswordDao

    /**
     * Initialize the repository with context.
     * Should be called once in Application class.
     */
    fun initialize(context: Context) {
        if (!::passwordDao.isInitialized) {
            val database = DatabaseProvider.getInstance(context)
            passwordDao = database.passwordDao()
        }
    }

    /**
     * Observe all passwords (returns Flow)
     */
    fun getPasswords(): Flow<List<Password>> {
        checkInitialized()
        return passwordDao.getAllPasswords()
    }

    /**
     * Get a single password by ID (suspend)
     */
    fun getPassword(id: String?): Password? {
        if (id == null) return null
        checkInitialized()
        return passwordDao.getPasswordById(id)
    }

    /**
     * Insert or update a password
     */
    fun savePassword(password: Password) {
        checkInitialized()
        passwordDao.insertOrUpdate(password)
    }

    /**
     * Delete a password
     */
    fun deletePassword(password: Password) {
        checkInitialized()
        passwordDao.delete(password)
    }

    /**
     * Delete all passwords (use with caution)
     */
    fun deleteAllData() {
        checkInitialized()
        passwordDao.deleteAll()
    }

    /**
     * Throws exception if repository not initialized
     */
    private fun checkInitialized() {
        if (!::passwordDao.isInitialized) {
            throw IllegalStateException("PasswordRepository not initialized. Call initialize() first.")
        }
    }
}
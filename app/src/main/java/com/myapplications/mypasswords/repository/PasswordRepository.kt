package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.database.AppDatabase
import com.myapplications.mypasswords.database.PasswordDao
import com.myapplications.mypasswords.model.Password
import kotlinx.coroutines.flow.Flow

// The repository is now an object (singleton)
object PasswordRepository {

    private lateinit var passwordDao: PasswordDao

    // Call this from your Application or MainActivity class
    fun initialize(context: Context) {
        if (!::passwordDao.isInitialized) {
            val database = AppDatabase.getInstance(context)
            passwordDao = database.passwordDao()
        }
    }

    fun getPasswords(): Flow<List<Password>> {
        return passwordDao.getAllPasswords()
    }

    suspend fun getPassword(id: String?): Password? {
        if (id == null) return null
        return passwordDao.getPasswordById(id)
    }

    suspend fun savePassword(password: Password) {
        passwordDao.insertOrUpdate(password)
    }

    suspend fun deletePassword(password: Password) {
        passwordDao.delete(password)
    }

    suspend fun deleteAllData() {
        passwordDao.deleteAll()
    }
}
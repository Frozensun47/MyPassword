package com.myapplications.mypasswords.repository

import android.content.Context
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.security.SecurityManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class PasswordRepository {

    fun getPasswords(context: Context): Flow<List<Password>> = flow {
        emit(SecurityManager(context).getPasswords())
    }

    fun getPassword(context: Context, id: String?): Password? {
        return SecurityManager(context).getPasswords().find { it.id == id }
    }

    fun savePassword(context: Context, password: Password) {
        SecurityManager(context).savePassword(password)
    }

    fun deletePassword(context: Context, password: Password) {
        SecurityManager(context).deletePassword(password)
    }
}
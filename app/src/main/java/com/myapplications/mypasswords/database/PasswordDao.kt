package com.myapplications.mypasswords.database

import androidx.room.*
import com.myapplications.mypasswords.model.Password
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAllPasswords(): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    suspend fun getPasswordById(id: String): Password?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(password: Password)

    @Delete
    suspend fun delete(password: Password)

    @Query("DELETE FROM passwords")
    suspend fun deleteAll()
}
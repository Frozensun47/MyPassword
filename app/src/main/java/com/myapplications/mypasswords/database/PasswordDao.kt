package com.myapplications.mypasswords.database

import androidx.room.*
import com.myapplications.mypasswords.model.Password
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(password: Password)

    @Update
    suspend fun updatePassword(password: Password)

    @Delete
    suspend fun deletePassword(password: Password)

    // Get all passwords that are NOT in any folder (at the root)
    @Query("SELECT * FROM passwords WHERE folderId IS NULL ORDER BY title ASC")
    fun getRootPasswords(): Flow<List<Password>>

    // Get all passwords within a specific folder
    @Query("SELECT * FROM passwords WHERE folderId = :folderId ORDER BY title ASC")
    fun getPasswordsInFolder(folderId: String): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE id = :passwordId")
    suspend fun getPasswordById(passwordId: String): Password?

    @Query("SELECT * FROM passwords")
    fun getAllPasswords(): Flow<List<Password>>

    @Query("DELETE FROM passwords")
    suspend fun deleteAll()

}
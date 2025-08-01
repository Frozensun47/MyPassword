package com.myapplications.mypasswords.database

import androidx.room.*
import com.myapplications.mypasswords.model.Password
import kotlinx.coroutines.flow.Flow

@Dao
interface PasswordDao {

    @Query("SELECT * FROM passwords ORDER BY title ASC")
    fun getAllPasswords(): Flow<List<Password>>

    @Query("SELECT * FROM passwords WHERE id = :id")
    fun getPasswordById(id: String): Password?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(password: Password)

    @Delete
    fun delete(password: Password)

    @Query("DELETE FROM passwords")
    fun deleteAll()
}
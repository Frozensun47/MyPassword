package com.myapplications.mypasswords.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.framework.SupportOpenHelperFactory
import com.myapplications.mypasswords.model.Password
import com.myapplications.mypasswords.security.SecurityManager

@Database(entities = [Password::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun passwordDao(): PasswordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            // Use double-checked locking to ensure thread safety.
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            // 1. Get the passphrase securely from SecurityManager.
            val securityManager = SecurityManager(context)
            val passphrase = securityManager.getDatabasePassphrase().toByteArray()

            // 2. Create the configuration for the encrypted database.
            val openParams = SupportSQLiteDatabase.OpenParams.Builder()
                .setCipherAlgorithm(SupportSQLiteDatabase.SQLITE_CIPHER_AES256_GCM) // Recommended
                .setKdfAlgorithm(SupportSQLiteDatabase.SQLITE_KDF_PBKDF2_HMAC_SHA512) // Recommended
                .setKey(passphrase)
                .build()

            // 3. Create the factory using the configuration.
            val factory = SupportOpenHelperFactory(openParams)

            // 4. Build the Room database instance.
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "mypasswords.db"
            )
                .openHelperFactory(factory) // Use the new factory
                .build()
        }
    }
}
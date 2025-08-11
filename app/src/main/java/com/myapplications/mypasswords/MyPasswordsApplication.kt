// FILE: com/myapplications/mypasswords/MyPasswordsApplication.kt
package com.myapplications.mypasswords

import android.app.Application
import android.util.Log
import com.myapplications.mypasswords.repository.PasswordRepository

class MyPasswordsApplication : Application() {
    private val TAG = "MyPasswordsApplication"

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate: Application starting.")
        // Load native library here as it's a quick, low-level operation.
        System.loadLibrary("sqlcipher")
        Log.d(TAG, "onCreate: SQLCipher library loaded.")

        // Start initializing the repository.
        PasswordRepository.initialize(this)
        Log.d(TAG, "onCreate: PasswordRepository initialization triggered.")
    }
}
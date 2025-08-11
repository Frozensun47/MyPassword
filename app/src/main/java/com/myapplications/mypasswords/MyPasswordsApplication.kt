// FILE: com/myapplications/mypasswords/MyPasswordsApplication.kt
package com.myapplications.mypasswords

import android.app.Application
import com.myapplications.mypasswords.repository.PasswordRepository

class MyPasswordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Load native library here as it's a quick, low-level operation.
        // The repository initialization has been moved to MainActivity.
        System.loadLibrary("sqlcipher")
    }
}

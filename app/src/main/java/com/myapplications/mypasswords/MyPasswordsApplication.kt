package com.myapplications.mypasswords

import android.app.Application

class MyPasswordsApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        System.loadLibrary("sqlcipher")
    }
}

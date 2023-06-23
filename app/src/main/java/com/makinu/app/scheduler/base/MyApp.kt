package com.makinu.app.scheduler.base

import android.app.Application
import android.content.Context
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
    }

    companion object {
        private const val TAG = "MyApp"

        var instance: MyApp? = null
            private set

        val context: Context?
            get() = instance
    }
}
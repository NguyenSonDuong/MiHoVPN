package com.minhhoang

import android.util.Log
import androidx.multidex.MultiDexApplication
//import com.crashlytics.android.Crashlytics
//import com.crashlytics.android.core.CrashlyticsCore
import com.google.firebase.crashlytics.FirebaseCrashlytics
//import io.fabric.sdk.android.Fabric
import com.minhhoang.ui.Countries

class MainApplication : MultiDexApplication() {
    val appContainer = AppContainer()

    override fun onCreate() {
        setupLogging()
        super.onCreate()
        Countries.loadBitmaps()
        Log.i(TAG, "Application started")
    }

    private fun setupLogging() {
        FirebaseCrashlytics.getInstance().setCustomKey("current_level", 1)
    }

    companion object {
        private const val TAG = "MainApplication"
    }
}


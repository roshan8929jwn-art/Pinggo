package com.example

import android.app.Application
import android.util.Log
import com.example.util.FirebaseInitializer

class PinggoApplication : Application() {
  override fun onCreate() {
    super.onCreate()
    try {
      FirebaseInitializer.initialize(this)
    } catch (t: Throwable) {
      Log.e("PinggoApplication", "Startup error in PinggoApplication onCreate: ${t.message}", t)
    }
  }
}


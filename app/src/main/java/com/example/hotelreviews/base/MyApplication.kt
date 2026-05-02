package com.example.hotelreviews.base

import android.app.Application
import android.content.Context
import android.os.Handler
import android.os.Looper
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MyApplication : Application() {
    object Globals {
        var appContext: Context? = null
        var executorService: ExecutorService = Executors.newFixedThreadPool(4)
        var mainHandler: Handler = Handler(Looper.getMainLooper())
    }

    override fun onCreate() {
        super.onCreate()
        Globals.appContext = applicationContext
    }
}

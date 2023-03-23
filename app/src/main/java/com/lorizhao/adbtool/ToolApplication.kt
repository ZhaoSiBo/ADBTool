package com.lorizhao.adbtool

import android.app.Application
import timber.log.Timber

class ToolApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
    }
}
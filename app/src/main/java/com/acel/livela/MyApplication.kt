package com.acel.livela

import android.app.Application
import org.jetbrains.anko.AnkoLogger

class MyApplication : Application(), AnkoLogger {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

    }
}
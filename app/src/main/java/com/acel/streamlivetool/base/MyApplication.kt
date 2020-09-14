package com.acel.streamlivetool.base

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex

class MyApplication : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitiation.getInstance().init()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}
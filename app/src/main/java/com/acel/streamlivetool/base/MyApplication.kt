package com.acel.streamlivetool.base

import android.app.Application
import android.content.Context
import androidx.multidex.MultiDex
import com.acel.streamlivetool.util.PreferenceVariable

class MyApplication : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitiation.getInstance().init()
        PreferenceVariable.init()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(base)
    }
}
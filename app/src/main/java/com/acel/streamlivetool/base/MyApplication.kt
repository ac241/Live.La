package com.acel.streamlivetool.base

import android.app.Application
import com.acel.streamlivetool.util.AppInitiation
import org.jetbrains.anko.AnkoLogger

class MyApplication : Application(), AnkoLogger {
    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitiation.getInstance(this).init()
    }


}
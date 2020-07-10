package com.acel.streamlivetool.base

import android.app.Application
import android.os.Handler
import android.os.Looper
import com.acel.streamlivetool.util.AppInitiation
import org.jetbrains.anko.AnkoLogger

class MyApplication : Application(), AnkoLogger {
    companion object {
        lateinit var application: Application

        fun runOnUiThread(todo: () -> Unit) {
            Handler(Looper.getMainLooper()).post {
                todo.invoke()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitiation.getInstance(this).init()
    }


}
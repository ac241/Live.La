package com.acel.livela

import android.app.Application
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MyApplication:Application(),AnkoLogger {
    override fun onCreate() {
        super.onCreate()
    }
}
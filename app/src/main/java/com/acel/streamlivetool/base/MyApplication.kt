package com.acel.streamlivetool.base

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.util.AppInitiation

class MyApplication : Application() {
    companion object {
        lateinit var application: Application
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitiation.getInstance(this).init()
    }

}
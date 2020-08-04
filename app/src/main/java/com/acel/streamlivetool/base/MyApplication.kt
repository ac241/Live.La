package com.acel.streamlivetool.base

import android.app.Application
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.util.AppInitiation

class MyApplication : Application() {
    companion object {
        lateinit var application: Application

        private val activityManageList = mutableListOf<AppCompatActivity>()
        fun addActivityToManageList(activity: AppCompatActivity) {
            activityManageList.add(activity)
        }

        fun removeActivityFromManageList(activity: AppCompatActivity) {
            activityManageList.remove(activity)
        }

        fun finishAllActivity() {
            activityManageList.forEach {
                it.finish()
            }
        }

        fun isActivityFirst(activity: AppCompatActivity): Boolean {
            val index = activityManageList.indexOf(activity)
            if (index == 0)
                return true
            return false
        }
    }

    override fun onCreate() {
        super.onCreate()
        application = this
        AppInitiation.getInstance(this).init()
    }


}
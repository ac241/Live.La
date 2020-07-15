package com.acel.streamlivetool.base

import android.app.Application
import android.util.Log
import com.acel.streamlivetool.util.AppInitiation

class MyApplication : Application() {
    companion object {
        lateinit var application: Application

        private val activityManageList = mutableListOf<BaseActivity>()
        fun addActivityToManageList(activity: BaseActivity) {
            activityManageList.add(activity)
        }

        fun removeActivityFromManageList(activity: BaseActivity) {
            activityManageList.remove(activity)
        }

        fun finishAllActivity() {
            activityManageList.forEach {
                it.finish()
            }
        }

        fun isActivityFirst(activity: BaseActivity): Boolean {
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
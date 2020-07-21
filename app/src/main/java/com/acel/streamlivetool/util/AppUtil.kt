package com.acel.streamlivetool.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.SharedPreferences
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher

val defaultSharedPreferences: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(MyApplication.application)
}
object AppUtil {
    fun runOnUiThread(todo: () -> Unit) {
        Handler(Looper.getMainLooper()).post {
            todo.invoke()
        }
    }
    fun startApp(context: Context, anchor: Anchor) {
        MainExecutor.execute {
            val platformImpl = PlatformDispatcher.getPlatformImpl(anchor.platform)
            try {
                platformImpl?.startApp(context, anchor)
            } catch (e: ActivityNotFoundException) {
                e.printStackTrace()
                runOnUiThread {
                    ToastUtil.toast(
                        "没有找到" +
                                platformImpl?.platformShowNameRes?.let { it1 ->
                                    MyApplication.application.resources.getString(
                                        it1
                                    )
                                }
                                + " app..."
                    )
                }
            }
        }
    }
}
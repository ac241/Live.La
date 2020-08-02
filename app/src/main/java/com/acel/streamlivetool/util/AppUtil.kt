package com.acel.streamlivetool.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import com.acel.streamlivetool.R
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
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    ToastUtil.toast(
                        MyApplication.application.resources.getString(
                            R.string.did_not_find_app,
                            platformImpl?.platformShowNameRes?.let { it1 ->
                                MyApplication.application.resources.getString(
                                    it1
                                )
                            })
                    )
                }
            }
        }
    }

    /**
     * 判断Wifi是否连接
     */
    @Suppress("DEPRECATION")
    fun isWifiConnected(): Boolean {
        val connectivityManager =
            MyApplication.application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val activeNetInfo = connectivityManager.activeNetworkInfo
        return activeNetInfo != null && activeNetInfo.type == ConnectivityManager.TYPE_WIFI
    }
}
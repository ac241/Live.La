package com.acel.streamlivetool.util

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import androidx.preference.PreferenceManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException


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
                            platformImpl?.platformName
                        )
                    )
                }
            }
        }
    }

    /**
     * 重启应用
     */
    fun restartApp() {
        val intent =
            MyApplication.application.packageManager.getLaunchIntentForPackage(MyApplication.application.packageName)
        intent?.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        MyApplication.application.startActivity(intent)
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

    /**
     * 获取进程号对应的进程名
     *
     * @param pid 进程号
     * @return 进程名
     */
    internal fun getProcessName(pid: Int): String? {
        var reader: BufferedReader? = null
        try {
            reader = BufferedReader(FileReader("/proc/$pid/cmdline"))
            var processName: String = reader.readLine()
            if (!TextUtils.isEmpty(processName)) {
                processName = processName.trim { it <= ' ' }
            }
            return processName
        } catch (throwable: Throwable) {
            throwable.printStackTrace()
        } finally {
            try {
                reader?.close()
            } catch (exception: IOException) {
                exception.printStackTrace()
            }
        }
        return null
    }

}
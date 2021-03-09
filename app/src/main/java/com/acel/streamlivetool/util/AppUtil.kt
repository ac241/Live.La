package com.acel.streamlivetool.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.os.Handler
import android.os.Looper
import androidx.preference.PreferenceManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.main.MainActivity
import com.bumptech.glide.util.Util
import kotlin.system.exitProcess


val defaultSharedPreferences: SharedPreferences by lazy {
    PreferenceManager.getDefaultSharedPreferences(MyApplication.application)
}

object AppUtil {
    fun mainThread(todo: () -> Unit) {
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
                mainThread {
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

    fun restartAppBackup() {
        val mStartActivity = Intent(MyApplication.application, MainActivity::class.java)
        val mPendingIntentId = 123456
        val mPendingIntent = PendingIntent.getActivity(
            MyApplication.application,
            mPendingIntentId,
            mStartActivity,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val arm = MyApplication.application.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        arm[AlarmManager.RTC, System.currentTimeMillis() + 100] = mPendingIntent
        exitProcess(0)
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
     * 返回当前程序版本号
     */
    fun getAppName(context: Context): String? {
        var appName = ""
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            appName = pi.applicationInfo.loadLabel(context.packageManager).toString()
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return appName
    }

    /**
     * 返回当前程序版本号
     */
    fun getAppVersionCode(context: Context): String? {
        var versioncode = 0
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            // versionName = pi.versionName;
            versioncode = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                pi.longVersionCode.toInt()
            } else
                pi.versionCode
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return versioncode.toString() + ""
    }

    /**
     * 返回当前程序版本名
     */
    fun getAppVersionName(context: Context): String? {
        var versionName: String? = null
        try {
            val pm = context.packageManager
            val pi = pm.getPackageInfo(context.packageName, 0)
            versionName = pi.versionName
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return versionName
    }

    /**
     * Throws an [java.lang.IllegalArgumentException] if called on a thread other than the main
     * thread.
     */
    fun assertMainThread() {
        require(Util.isOnMainThread()) { "You must call this method on the main thread" }
    }

    /** Throws an [java.lang.IllegalArgumentException] if called on the main thread.  */
    fun assertBackgroundThread() {
        require(Util.isOnBackgroundThread()) { "You must call this method on a background thread" }
    }

    /** Returns `true` if called on the main thread, `false` otherwise.  */
    fun isOnMainThread(): Boolean {
        return Looper.myLooper() == Looper.getMainLooper()
    }
}
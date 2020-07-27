package com.acel.streamlivetool.util

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.util.ToastUtil.toast
import com.baidu.mobstat.StatService
import com.tencent.bugly.crashreport.CrashReport

class AppInitiation {
    companion object {
        lateinit var appContext: Context
        fun getInstance(context: Context): AppInitiation {
            appContext = context
            return AppInitiation()
        }
    }

    private val fullVersion: Boolean = false
    private val buglyAppId = "ee4f2df64b"

    fun init() {
        firstTimeLaunch()
        initPreference()
        initBugly()
        initMtj()
        if (fullVersion)
            toast("当前处于完整模式！")
    }

    /**
     * 百度统计
     */
    private fun initMtj() {
        StatService.setAuthorizedState(appContext, false)
        StatService.start(appContext)
    }

    private fun initBugly() {
        val strategy = CrashReport.UserStrategy(appContext)
        CrashReport.initCrashReport(appContext, buglyAppId, false, strategy)
    }

    private fun firstTimeLaunch() {
        val firstTimeKey = appContext.getString(R.string.string_first_time_launch)
        val isFirst = defaultSharedPreferences.getBoolean(firstTimeKey, true)
        if (isFirst) {
            initDefaultAnchor()
            initFullVersion()
            defaultSharedPreferences.edit().putBoolean(firstTimeKey, false).apply()
        }
    }

    private fun initFullVersion() {
        //是否使用完整版
        defaultSharedPreferences.edit()
            .putBoolean(appContext.resources.getString(R.string.full_version), fullVersion).apply()
    }

    private fun initPreference() {
        val isInitialed = defaultSharedPreferences.getBoolean(
            appContext.getString(R.string.string_preference_initialed),
            false
        )
        if (!isInitialed) {
            PreferenceManager.setDefaultValues(appContext, R.xml.pre_settings, false)
            defaultSharedPreferences.edit().putBoolean(
                appContext.getString(R.string.string_preference_initialed),
                true
            ).apply()
        }
    }

    private fun initDefaultAnchor() {
        Log.d("initDefaultAnchor", "init")
        val anchorRepository = AnchorRepository.getInstance(appContext.applicationContext)
        val list = mutableListOf<Anchor>()
        list.add(Anchor("douyu", "即将拥有人鱼线的PDD", "101", "101"))
        list.add(Anchor("douyu", "英雄联盟赛事", "288016", "288016"))
        list.add(Anchor("douyu", "毛阿姨不在", "469195", "469195"))
        list.add(Anchor("douyu", "女流66", "156277", "156277"))
        list.add(Anchor("bilibili", "阿P在家吗", "12856139", "12856139"))
        list.add(Anchor("bilibili", "超carry的柴西", "21426464", "21426464"))
        list.add(Anchor("douyu", "小苏菲", "241431", "241431"))
        list.add(Anchor("bilibili", "凉子和猫", "1039633", "1039633"))
        list.forEach {
            anchorRepository.insertAnchor(it)
        }
    }
}
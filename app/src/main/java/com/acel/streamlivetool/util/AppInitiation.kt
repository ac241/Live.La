package com.acel.streamlivetool.util

import android.content.Context
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.util.AppUtil.defaultSharedPreferences
import com.tencent.bugly.crashreport.CrashReport

class AppInitiation {
    companion object {
        lateinit var appContext: Context
        fun getInstance(context: Context): AppInitiation {
            appContext = context
            return AppInitiation()
        }
    }

    private val buglyAppId = "ee4f2df64b"

    fun init() {
        firstTimeLaunch()
        initBugly()
    }

    private fun initBugly() {
        val strategy = CrashReport.UserStrategy(appContext)
        CrashReport.initCrashReport(appContext, buglyAppId, false, strategy)
    }

    private fun firstTimeLaunch() {
        val firstKey = "first_time_launch"
        val isFirst = defaultSharedPreferences.getBoolean(firstKey, true)
        if (isFirst) {
            initDefaultAnchor()
            initPreference()
            initFullVersion()
            defaultSharedPreferences.edit().putBoolean(firstKey, false).apply()
        }
    }

    private fun initFullVersion() {
        //是否使用完整版
        defaultSharedPreferences.edit()
            .putBoolean(appContext.resources.getString(R.string.full_version), false).apply()
    }

    private fun initPreference() {
        defaultSharedPreferences.edit()
            .putString(appContext.getString(R.string.pref_key_item_click_action), "open_app")
            .putString(
                appContext.getString(R.string.pref_key_second_button_click_action),
                "outer_player"
            )
            .apply()
    }

    private fun initDefaultAnchor() {
        val anchorRepository = AnchorRepository.getInstance(appContext.applicationContext)
        val list = mutableListOf<Anchor>()
        list.add(Anchor("douyu", "即将拥有人鱼线的PDD", "101", "101"))
        list.add(Anchor("douyu", "毛阿姨", "469195", "469195"))
        list.add(Anchor("bilibili", "阿P在家吗", "12856139", "12856139"))
        list.add(Anchor("bilibili", "超carry的柴西", "21426464", "21426464"))
        list.add(Anchor("douyu", "小苏菲", "241431", "241431"))
        list.add(Anchor("bilibili", "凉子和猫", "1039633", "1039633"))
        list.forEach {
            anchorRepository.insertAnchor(it)
        }
    }
}
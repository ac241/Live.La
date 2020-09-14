package com.acel.streamlivetool.base

import android.util.Log
import androidx.preference.PreferenceManager
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.AnchorRepository
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences

/**
 * 初始化工具
 */
class AppInitiation {
    companion object {
        fun getInstance(): AppInitiation {
            return AppInitiation()
        }
    }

    private val isDebug: Boolean = false
    private val isFullVersion: Boolean = false


    fun init() {
        initPreference()
        firstTimeLaunch()
        if (isDebug) {
            toast("当前处于测试模式！")
            defaultSharedPreferences.edit()
                .putBoolean(
                    MyApplication.application.resources.getString(R.string.full_version),
                    true
                ).apply()
        }
    }

    private fun firstTimeLaunch() {
        val firstTimeKey = MyApplication.application.getString(R.string.string_first_time_launch)
        val isFirst = defaultSharedPreferences.getBoolean(firstTimeKey, true)
        if (isFirst) {
            initDefaultAnchor()
            initFullVersion()
            defaultSharedPreferences.edit().putBoolean(firstTimeKey, false).apply()
        }
    }

    private fun initFullVersion() {
        defaultSharedPreferences.edit()
            .putBoolean(
                MyApplication.application.resources.getString(R.string.full_version),
                isFullVersion
            ).apply()
    }

    private fun initPreference() {
        PreferenceManager.setDefaultValues(MyApplication.application, R.xml.pre_settings, false)
        defaultSharedPreferences.edit().putBoolean(
            MyApplication.application.getString(R.string.string_preference_initialed),
            true
        ).apply()
    }

    private fun initDefaultAnchor() {
        Log.d("initDefaultAnchor", "init")
        val anchorRepository = AnchorRepository.getInstance()
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
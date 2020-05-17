package com.acel.streamlivetool

import android.app.Application
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.db.DbManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.defaultSharedPreferences

class MyApplication : Application(), AnkoLogger {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        firstTimeLaunch()
    }

    private fun firstTimeLaunch() {
        val firstKey = "first_time_launch"
        val isFirst = defaultSharedPreferences.getBoolean(firstKey, true)
        if (isFirst) {
            initDefaultAnchor()
            initPreference()
            defaultSharedPreferences.edit().putBoolean(firstKey, false).apply()
        }
    }

    private fun initPreference() {
        defaultSharedPreferences.edit()
            .putString(getString(R.string.pref_key_item_click_action), "open_app")
            .putString(getString(R.string.pref_key_second_button_click_action), "outer_player")
            .apply()

    }

    private fun initDefaultAnchor() {
        val anchorDao = DbManager.getInstance(this)?.getDaoSession(this)?.anchorDao
        val list = mutableListOf<Anchor>()
        list.add(Anchor("douyu", "即将拥有人鱼线的PDD", "101", "101"))
        list.add(Anchor("douyu", "小苏菲", "241431", "241431"))
        list.add(Anchor("bilibili", "阿P在家吗", "12856139", "12856139"))
        list.add(Anchor("bilibili", "凉子和猫", "1039633", "1039633"))
        list.add(Anchor("douyu", "Nymph佩佩", "5122899", "5122899"))
        list.add(Anchor("douyu", "毛阿姨", "469195", "469195"))
        list.forEach {
            anchorDao?.insert(it)
        }
    }
}
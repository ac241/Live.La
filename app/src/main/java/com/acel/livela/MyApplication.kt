package com.acel.livela

import android.app.Application
import android.preference.PreferenceManager
import android.util.Log
import com.acel.livela.bean.Anchor
import com.acel.livela.db.DbManager
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.defaultSharedPreferences
import java.util.*

class MyApplication : Application(), AnkoLogger {
    companion object {
        lateinit var instance: Application
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        firstTimeLaunch()
    }

    fun firstTimeLaunch() {
        val first_key = "first_time_launch"
        val isFirst = defaultSharedPreferences.getBoolean(first_key, true)
        if (isFirst) {
            initDefaultAnchor()
            initPreference()
            defaultSharedPreferences.edit().putBoolean(first_key, false).apply()
        }
    }

    private fun initPreference() {
        defaultSharedPreferences.edit()
            .putString(getString(R.string.pref_key_item_click_action), "inner_player")
            .putString(getString(R.string.pref_key_second_button_click_action), "open_app")
            .apply()

    }

    private fun initDefaultAnchor() {
        val anchorDao = DbManager.getInstance(this)?.getDaoSession(this)?.anchorDao
        val list = mutableListOf<Anchor>()
        list.add(Anchor("douyu", "毛阿姨", "469195", "469195"))
        list.add(Anchor("bilibili", "阿P在家吗", "12856139", "12856139"))
        list.add(Anchor("douyu", "小苏菲", "241431", "241431"))
        list.forEach {
            anchorDao?.insert(it)
        }
    }
}
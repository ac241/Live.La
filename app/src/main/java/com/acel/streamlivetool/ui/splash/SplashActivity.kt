package com.acel.streamlivetool.ui.splash

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

class SplashActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun init() {
        val mode = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_launch_activity),
            getString(R.string.group_mode)
        )
        when (mode) {
            getString(R.string.group_mode) ->
                startActivity<GroupModeActivity>()
            getString(R.string.cookie_mode) ->
                startActivity<CookieModeActivity>()
        }
        finish()
    }
}
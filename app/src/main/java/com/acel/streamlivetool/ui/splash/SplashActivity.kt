package com.acel.streamlivetool.ui.splash

import android.content.Intent
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.ui.cookie_mode.CookieModeActivity
import com.acel.streamlivetool.ui.group_mode.GroupModeActivity
import com.acel.streamlivetool.util.defaultSharedPreferences

class SplashActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun createdDo() {
        val mode = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_launch_activity),
            getString(R.string.group_mode)
        )
        when (mode) {
            getString(R.string.group_mode) ->
                startActivity(Intent(this, GroupModeActivity::class.java))
            getString(R.string.cookie_mode) ->
                startActivity(Intent(this, CookieModeActivity::class.java))
            else ->
                startActivity(Intent(this, GroupModeActivity::class.java))
        }
        finish()
    }
}
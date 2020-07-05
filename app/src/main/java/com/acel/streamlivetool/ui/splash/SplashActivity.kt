package com.acel.streamlivetool.ui.splash

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.ui.cookie_anchor.CookieModeActivity
import com.acel.streamlivetool.ui.main.MainActivity
import org.jetbrains.anko.defaultSharedPreferences
import org.jetbrains.anko.startActivity

class SplashActivity : BaseActivity() {
    override fun getResLayoutId(): Int {
        return R.layout.activity_splash
    }

    override fun init() {
        val mode = defaultSharedPreferences.getString(
            resources.getString(R.string.pref_key_launch_activity),
            "main_activity"
        )
        when (mode) {
            "main_activity" ->
                startActivity<MainActivity>()
            "cookie_activity" ->
                startActivity<CookieModeActivity>()
        }
        finish()
    }
}
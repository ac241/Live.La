package com.acel.streamlivetool.ui.settings

import android.content.Intent
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.ui.splash.SplashActivity

class SettingsActivity : BaseActivity() {
    var settingsChanges = false
    override fun getResLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun init() {
        supportFragmentManager.beginTransaction().add(R.id.setting_layout, SettingsFragment())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settingsChanges) {
            MyApplication.finishAllActivity()
            startActivity(Intent(this, SplashActivity::class.java))
        }
//        restartApplication(this)
    }
}

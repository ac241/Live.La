package com.acel.streamlivetool.ui.settings

import android.content.Intent
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.ui.splash.SplashActivity

class SettingsActivity : BaseActivity() {
    var settingsChanges = false
    override fun getResLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun createdDo() {
        supportFragmentManager.beginTransaction().add(R.id.setting_layout, SettingsFragment())
            .commit()
    }

    override fun destroyDo() {
        if (settingsChanges) {
            MyApplication.finishAllActivity()
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}

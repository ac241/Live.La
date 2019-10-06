package com.acel.streamlivetool.ui.settings

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun getResLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun init() {
        supportFragmentManager.beginTransaction().add(R.id.setting_layout, SettingsFragment()).commit()
    }
}

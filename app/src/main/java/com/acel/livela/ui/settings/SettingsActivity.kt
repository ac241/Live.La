package com.acel.livela.ui.settings

import com.acel.livela.R
import com.acel.livela.base.BaseActivity

class SettingsActivity : BaseActivity() {

    override fun getResLayoutId(): Int {
        return R.layout.activity_setting
    }

    override fun init() {
        supportFragmentManager.beginTransaction().add(R.id.setting_layout, SettingsFragment()).commit()
    }
}

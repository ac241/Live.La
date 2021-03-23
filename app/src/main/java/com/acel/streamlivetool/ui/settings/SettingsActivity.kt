package com.acel.streamlivetool.ui.settings

import android.os.Bundle
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity

class SettingsActivity : BaseActivity() {

    private val fragmentTag = "setting_fragment"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        val fragment = supportFragmentManager.findFragmentByTag(fragmentTag)
        fragment ?: supportFragmentManager.beginTransaction()
            .add(R.id.fragment_container, SettingsFragment(), "setting_fragment")
            .commit()
    }
}

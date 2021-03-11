package com.acel.streamlivetool.base

import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.util.defaultSharedPreferences

open class BaseActivity : AppCompatActivity() {
    private val windowInsetsController by lazy { ViewCompat.getWindowInsetsController(window.decorView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupNightMode()
    }

    private fun setupNightMode() {
        val enableNightMode = defaultSharedPreferences.getBoolean(
            getString(R.string.pref_key_enable_night_mode),
            false
        )
        val nightModeFollowSystem = defaultSharedPreferences.getBoolean(
            getString(R.string.pref_key_night_mode_follow_system),
            false
        )
        if (enableNightMode) {
            if (nightModeFollowSystem)
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            else
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.background_light, null)
            window.navigationBarColor = resources.getColor(R.color.background_light, null)
        }
        windowInsetsController?.isAppearanceLightStatusBars = !isNightMode()
    }

    fun isNightMode(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
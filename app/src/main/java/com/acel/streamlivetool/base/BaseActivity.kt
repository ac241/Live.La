package com.acel.streamlivetool.base

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import com.acel.streamlivetool.R

open class BaseActivity : AppCompatActivity() {
    private val windowInsetsController by lazy { ViewCompat.getWindowInsetsController(window.decorView) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            window.statusBarColor = resources.getColor(R.color.background_light, null)
            window.navigationBarColor = resources.getColor(R.color.background_light, null)
        }
        windowInsetsController?.isAppearanceLightStatusBars = !isNightMode()
    }

    fun isNightMode(): Boolean {
        return resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
    }
}
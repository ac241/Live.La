package com.acel.streamlivetool.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.ui.main.MainActivity
import com.acel.streamlivetool.util.AppUtil.restartApp
import com.acel.streamlivetool.util.ToastUtil.toast

class SettingsActivity : AppCompatActivity() {
    var settingsChanges = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportFragmentManager.beginTransaction().add(R.id.setting_layout, SettingsFragment())
            .commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (settingsChanges) {
            val intent = Intent(this, MainActivity::class.java)
            intent.action = MainActivity.OnNewIntentAction.PREF_CHANGED
            startActivity(intent)
//            restartApp()
        }
    }
}

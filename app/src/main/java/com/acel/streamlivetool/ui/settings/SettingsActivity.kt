package com.acel.streamlivetool.ui.settings

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.BaseActivity
import com.acel.streamlivetool.ui.main.MainActivity

class SettingsActivity : BaseActivity() {

    private val changesList = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        supportFragmentManager.beginTransaction().add(R.id.fragment_container, SettingsFragment())
            .commit()
    }

    fun setPlatformsChanges() {
        changesList.add(MainActivity.PREF_PLATFORMS_CHANGED)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (changesList.isNotEmpty()) {
            val intent = Intent(this, MainActivity::class.java)
            intent.action = MainActivity.ACTION_PREF_CHANGES
            intent.putStringArrayListExtra(
                MainActivity.EXTRA_KEY_PREF_CHANGES,
                changesList
            )
            startActivity(intent)
        }
    }
}

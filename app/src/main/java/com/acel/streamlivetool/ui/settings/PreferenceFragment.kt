package com.acel.streamlivetool.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.open_source.OpenSourceActivity
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.support.v4.startActivity
import org.jetbrains.anko.support.v4.toast

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceClickListener {
    private val entriesMap = mutableMapOf<String, Pair<Array<String>, Array<String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        entriesMap[getString(R.string.pref_key_item_click_action)] = Pair(
            resources.getStringArray(R.array.pref_click_action_entries),
            resources.getStringArray(R.array.pref_click_action_entry_values)
        )
        entriesMap[getString(R.string.pref_key_second_button_click_action)] = Pair(
            resources.getStringArray(R.array.pref_click_action_entries),
            resources.getStringArray(R.array.pref_click_action_entry_values)
        )
        entriesMap[getString(R.string.pref_key_launch_activity)] = Pair(
            resources.getStringArray(R.array.pref_launch_activity_entries),
            resources.getStringArray(R.array.pref_launch_activity_entries_values)
        )

        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.pre_settings)
        initPreferencesSummary()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        preferenceScreen.onPreferenceClickListener = this
        findPreference<Preference>(getString(R.string.pref_key_about_open_source))?.setOnPreferenceClickListener {
            startActivity<OpenSourceActivity>()
            false
        }
        findPreference<Preference>(getString(R.string.pref_key_clear_cookie))?.setOnPreferenceClickListener {
            clearCookie()

            false
        }
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) {
            when (key) {
                //改变ListPreference值时设置summary
                getString(R.string.pref_key_item_click_action) ->
                    setListPreferenceSummary(key)
                getString(R.string.pref_key_second_button_click_action) ->
                    setListPreferenceSummary(key)
                getString(R.string.pref_key_launch_activity) ->
                    setListPreferenceSummary(key)
            }
        }
    }

    override fun onPreferenceClick(p0: Preference?): Boolean {
//        when (p0?.key) {
//            getString(R.string.pref_key_about_open_source) ->
//                startActivity<OpenSourceActivity>()
//        }
        return true
    }

    private fun clearCookie() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder
            ?.setTitle("要清除登录信息吗？")
            ?.setPositiveButton("是") { _, _ ->
                val platforms = mutableListOf<IPlatform>().also {
                    for (entry in PlatformDispatcher.getAllPlatformInstance()) {
                        if (entry.value.supportCookieMode)
                            it.add(entry.value)
                    }
                }
                platforms.forEach {
                    it.clearCookie()
                }
                toast("清除cookie成功")
            }
            ?.setNegativeButton("否", null)
            ?.show()
    }

    private fun initPreferencesSummary() {
        setListPreferenceSummary(getString(R.string.pref_key_item_click_action))
        setListPreferenceSummary(getString(R.string.pref_key_second_button_click_action))
        setListPreferenceSummary(getString(R.string.pref_key_launch_activity))
    }

    private fun setListPreferenceSummary(key: String) {
        val value = defaultSharedPreferences.getString(key, "")
        val entries = entriesMap[key]?.first
        val values = entriesMap[key]?.second
        preferenceScreen.findPreference<Preference>(key)?.summary =
            values?.indexOf(value)?.let { entries?.get(it) }
    }
}
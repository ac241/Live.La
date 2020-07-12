package com.acel.streamlivetool.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.open_source.OpenSourceActivity
import com.acel.streamlivetool.util.defaultSharedPreferences
import com.acel.streamlivetool.util.ToastUtil.toast

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    private val entriesMap = mutableMapOf<String, Pair<Array<String>, Array<String>>>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        entriesMap[getString(R.string.pref_key_group_mode_list_type)] = Pair(
            resources.getStringArray(R.array.pref_group_mode_list_type_entries),
            resources.getStringArray(R.array.pref_group_mode_list_type_entries_values)
        )
        initPreferencesSummary()
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.pre_settings)
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)

        findPreference<Preference>(getString(R.string.pref_key_about_open_source))?.setOnPreferenceClickListener {
            startActivity(Intent(context, OpenSourceActivity::class.java))
            false
        }
        findPreference<Preference>(getString(R.string.pref_key_clear_cookie))?.setOnPreferenceClickListener {
            clearCookie()
            false
        }
        //隐藏preference
        val fullVersion =
            defaultSharedPreferences.getBoolean(resources.getString(R.string.full_version), false)
        if (!fullVersion) {
            val hideList = arrayOf(
                R.string.pref_key_item_click_action,
                R.string.pref_key_second_button_click_action
            )
            hideList.forEach {
                findPreference<Preference>(getString(it))?.isVisible = false
            }
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) {
            if (key != null) {
                setListPreferenceSummary(key)
            }
            when (key) {
                resources.getString(R.string.pref_key_group_mode_list_type), resources.getString(R.string.pref_key_launch_activity) ->
                    (requireActivity() as SettingsActivity).settingsChanges = true
            }
        }
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
        entriesMap.keys.forEach {
            setListPreferenceSummary(it)
        }
    }

    private fun setListPreferenceSummary(key: String) {
        val value = defaultSharedPreferences.getString(key, "")
        val entries = entriesMap[key]?.first
        val values = entriesMap[key]?.second
        preferenceScreen.findPreference<Preference>(key)?.summary =
            values?.indexOf(value)?.let { entries?.get(it) }
    }
}
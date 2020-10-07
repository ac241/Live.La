package com.acel.streamlivetool.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.open_source.OpenSourceActivity
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {
    /**
     * 设置后重启
     */
    private val restartWhenChangeArray by lazy {
        arrayOf(
            resources.getString(R.string.pref_key_additional_action_btn),
            resources.getString(R.string.pref_key_cookie_mode_platform_showable),
            resources.getString(R.string.pref_key_show_anchor_image_when_mobile_data),
            resources.getString(R.string.pref_key_show_anchor_image),
            resources.getString(R.string.pref_key_group_mode_use_cookie),
            resources.getString(R.string.pref_key_group_mode_use_cookie)
        )
    }
    private val entriesMap =
        mutableMapOf<String, Pair<Array<String>, Array<String>>>().also {
            MyApplication.application.resources.let { res ->
                it[res.getString(R.string.pref_key_item_click_action)] = Pair(
                    res.getStringArray(R.array.pref_click_action_entries),
                    res.getStringArray(R.array.pref_click_action_entry_values)
                )
                it[res.getString(R.string.pref_key_second_button_click_action)] = Pair(
                    res.getStringArray(R.array.pref_click_action_entries),
                    res.getStringArray(R.array.pref_click_action_entry_values)
                )
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        //隐藏完整版功能
        val fullVersion =
            defaultSharedPreferences.getBoolean(resources.getString(R.string.full_version), false)
        if (!fullVersion)
            findPreference<Preference>(getString(R.string.pref_key_full_version_feature))?.isVisible =
                false
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (isAdded) {
            if (key != null) {
                if (entriesMap.keys.contains(key))
                    setListPreferenceSummary(key)
            }
            //设置后重启应用
            if (restartWhenChangeArray.contains(key)) {
                (requireActivity() as SettingsActivity).settingsChanges = true
            }
        }
    }

    private fun clearCookie() {
        val builder = context?.let { AlertDialog.Builder(it) }
        builder
            ?.setTitle(getString(R.string.clear_all_cookie_alert))
            ?.setPositiveButton(getString(R.string.yes)) { _, _ ->
                val platforms = mutableListOf<IPlatform>().also {
                    for (entry in PlatformDispatcher.getAllPlatformInstance()) {
                        if (entry.value.supportCookieMode)
                            it.add(entry.value)
                    }
                }
                platforms.forEach {
                    it.clearCookie()
                }
                toast(getString(R.string.clear_all_cookie_ok))
            }
            ?.setNegativeButton(getString(R.string.no), null)
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
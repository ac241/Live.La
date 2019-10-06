package com.acel.streamlivetool.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.Preference
import android.support.v7.preference.PreferenceFragmentCompat
import com.acel.streamlivetool.R
import com.acel.streamlivetool.ui.open_source.OpenSourceActivity
import org.jetbrains.anko.support.v4.defaultSharedPreferences
import org.jetbrains.anko.support.v4.startActivity

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener,
    Preference.OnPreferenceClickListener {

    lateinit var entries: Array<String>
    lateinit var entryValues: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        entries = resources.getStringArray(R.array.pref_click_action_entries)
        entryValues = resources.getStringArray(R.array.pref_click_action_entry_values)
        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(p0: Bundle?, p1: String?) {
        addPreferencesFromResource(R.xml.pre_settings)
        initPreferencesSummary()
        preferenceScreen.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
        preferenceScreen.setOnPreferenceClickListener(this)
        findPreference(getString(R.string.pref_key_about_open_source)).setOnPreferenceClickListener {
            startActivity<OpenSourceActivity>()
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
            }
        }
    }

    override fun onPreferenceClick(p0: Preference?): Boolean {
        when (p0?.key) {
            getString(R.string.pref_key_about_open_source) ->
                startActivity<OpenSourceActivity>()
        }
        return true
    }

    private fun initPreferencesSummary() {
        setListPreferenceSummary(getString(R.string.pref_key_item_click_action))
        setListPreferenceSummary(getString(R.string.pref_key_second_button_click_action))
    }

    fun setListPreferenceSummary(key: String) {
        val value = defaultSharedPreferences.getString(key, "")
        preferenceScreen.findPreference(key).summary = entries[entryValues.indexOf(value)]
    }
}
package com.acel.livela.ui.settings

import android.content.SharedPreferences
import android.os.Bundle
import android.support.v7.preference.PreferenceFragmentCompat
import android.util.Log
import com.acel.livela.R
import org.jetbrains.anko.support.v4.defaultSharedPreferences

class SettingsFragment : PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {

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
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.d("ACEL_LOG", "key:" + key)
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

    private fun initPreferencesSummary() {
        setListPreferenceSummary(getString(R.string.pref_key_item_click_action))
        setListPreferenceSummary(getString(R.string.pref_key_second_button_click_action))
    }

    fun setListPreferenceSummary(key: String) {
        val value = defaultSharedPreferences.getString(key, "")
        preferenceScreen.findPreference(key).summary = entries[entryValues.indexOf(value)]
    }
}
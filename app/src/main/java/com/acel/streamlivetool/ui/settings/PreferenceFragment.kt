package com.acel.streamlivetool.ui.settings

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import androidx.preference.forEach
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.PlatformDispatcher
import com.acel.streamlivetool.ui.custom.AlertDialogTool
import com.acel.streamlivetool.ui.open_source.OpenSourceActivity
import com.acel.streamlivetool.util.AppUtil.getAppName
import com.acel.streamlivetool.util.AppUtil.getAppVersionName
import com.acel.streamlivetool.util.PreferenceVariable
import com.acel.streamlivetool.util.ToastUtil.toast
import com.acel.streamlivetool.util.defaultSharedPreferences

class SettingsFragment : PreferenceFragmentCompat(),
    SharedPreferences.OnSharedPreferenceChangeListener {

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
        preferenceScreen.forEach {

        }
        findPreference<Preference>(getString(R.string.pref_key_about_open_source))?.setOnPreferenceClickListener {
            startActivity(Intent(context, OpenSourceActivity::class.java))
            true
        }
        findPreference<Preference>(getString(R.string.pref_key_clear_cookie))?.setOnPreferenceClickListener {
            clearCookie()
            true
        }
        findPreference<Preference>(getString(R.string.pref_key_app_description))?.apply {
            title = "${getAppName(requireContext())} ${getAppVersionName(requireContext())}"
            setOnPreferenceClickListener {
                AlertDialogTool.newAlertDialog(requireContext())
                    .setTitle("项目地址：")
                    .setMessage(getString(R.string.github_url))
                    .show()
                true
            }
        }

        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_enable_night_mode))?.setOnPreferenceChangeListener { _, newValue ->
            newValue as Boolean
            requireActivity().recreate()
            true
        }
        findPreference<SwitchPreferenceCompat>(getString(R.string.pref_key_night_mode_follow_system))?.setOnPreferenceChangeListener { _, newValue ->
            newValue as Boolean
            requireActivity().recreate()
            true
        }
        findPreference<Preference>(getString(R.string.pref_key_huya_danmu_data))?.setOnPreferenceClickListener {
            dialogHuyaDanmuData()
            true
        }
    }

    private fun dialogHuyaDanmuData() {
        val dialog = AlertDialogTool.newAlertDialog(requireContext())
            .setTitle(R.string.huya_danmu_data)
            .setView(R.layout.layout_huya_danmu_data)
            .setNegativeButton("取消", null)
            .create()

        dialog.apply {
            setOnShowListener {
                val appIdView = dialog.findViewById<EditText>(R.id.appId)
                val secretView = dialog.findViewById<EditText>(R.id.secret)
                val spAppId = defaultSharedPreferences.getString(
                    getString(R.string.key_huya_danmu_app_id),
                    ""
                )
                val spSecret = defaultSharedPreferences.getString(
                    getString(R.string.key_huya_danmu_secret),
                    ""
                )
                if (!spAppId.isNullOrEmpty() && !spSecret.isNullOrEmpty()) {
                    appIdView?.setText(spAppId)
                    secretView?.setText(spSecret)
                }
                findViewById<Button>(R.id.save)?.setOnClickListener {
                    val appId =
                        appIdView?.text.toString()
                    val secret =
                        secretView?.text.toString()
                    if (appId.isEmpty()) {
                        appIdView?.error = "请填写"
                        return@setOnClickListener
                    }
                    if (secret.isEmpty()) {
                        secretView?.error = "请填写"
                        return@setOnClickListener
                    }
                    defaultSharedPreferences.edit()
                        .putString(getString(R.string.key_huya_danmu_app_id), appId)
                        .putString(getString(R.string.key_huya_danmu_secret), secret)
                        .apply()
                    dialog.dismiss()
                    toast("保存成功")
                }
                findViewById<TextView>(R.id.how_to_get_huya_danmu_data)?.setOnClickListener {
                    dialog.findViewById<WebView>(R.id.webView)?.apply {
                        if (visibility == View.GONE) {
                            visibility = View.VISIBLE
                            loadUrl("file:///android_asset/how_to_get_huya_developer_data.html")
                        } else {
                            visibility = View.GONE
                        }
                    }
                }

            }

        }
        dialog.show()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        key?.let {
            PreferenceVariable.update(key)
        }
        if (isAdded) {
            if (key != null) {
                if (entriesMap.keys.contains(key))
                    setListPreferenceSummary(key)
            }
        }
    }

    private fun clearCookie() {
        val builder = context?.let { AlertDialogTool.newAlertDialog(it) }
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
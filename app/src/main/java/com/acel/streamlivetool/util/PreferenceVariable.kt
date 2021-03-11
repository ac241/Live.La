package com.acel.streamlivetool.util

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication

object PreferenceVariable {

    private val keySet = setOf(
        getKey(R.string.pref_key_show_anchor_image),
        getKey(R.string.pref_key_show_anchor_image_when_mobile_data),
        getKey(R.string.pref_key_cookie_mode_platform_displayable),
        getKey(R.string.pref_key_additional_action_btn),
        getKey(R.string.pref_key_item_click_action),
        getKey(R.string.pref_key_second_button_click_action),
        getKey(R.string.pref_key_group_mode_use_cookie)
    )

    var showAnchorImage = false
    var showAnchorImageWhenMobileData = false
    var displayablePlatformSet: Set<String>? = null
    var showAdditionalActionButton = false
    var itemClickAction = ""
    var secondaryButtonClickAction = ""
    var groupUseCookie = false

    fun init() {
        for (s in keySet) {
            update(s)
        }
    }

    private fun getKey(int: Int): String = MyApplication.application.getString(int)


    private fun getPreferenceBoolean(key: String): Boolean {
        return defaultSharedPreferences.getBoolean(key, false)
    }

    private fun getPreferenceString(key: String): String {
        return defaultSharedPreferences.getString(key, "") ?: ""
    }

    fun update(key: String) {
        when (key) {
            getKey(R.string.pref_key_show_anchor_image) ->
                showAnchorImage = getPreferenceBoolean(key)
            getKey(R.string.pref_key_show_anchor_image_when_mobile_data) ->
                showAnchorImageWhenMobileData = getPreferenceBoolean(key)
            getKey(R.string.pref_key_cookie_mode_platform_displayable) ->
                displayablePlatformSet = defaultSharedPreferences.getStringSet(key, setOf())
            getKey(R.string.pref_key_additional_action_btn) ->
                showAdditionalActionButton = getPreferenceBoolean(key)
            getKey(R.string.pref_key_item_click_action) ->
                itemClickAction = getPreferenceString(key)
            getKey(R.string.pref_key_second_button_click_action) ->
                secondaryButtonClickAction = getPreferenceString(key)
            getKey(R.string.pref_key_group_mode_use_cookie) ->
                groupUseCookie = getPreferenceBoolean(key)
        }
    }
}
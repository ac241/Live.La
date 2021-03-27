package com.acel.streamlivetool.const_value

import androidx.lifecycle.MutableLiveData
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.util.defaultSharedPreferences

@Suppress("MemberVisibilityCanBePrivate")
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

    var showAnchorImage = MutableLiveData(false)
    var showAnchorImageWhenMobileData = MutableLiveData(false)
    var displayablePlatformSet = MutableLiveData<Set<String>>()
    var showAdditionalActionButton = MutableLiveData(false)
    var itemClickAction = MutableLiveData("")
    var secondaryButtonClickAction = MutableLiveData("")
    var groupUseCookie = MutableLiveData(false)

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
                showAnchorImage.postValue(getPreferenceBoolean(key))
            getKey(R.string.pref_key_show_anchor_image_when_mobile_data) ->
                showAnchorImageWhenMobileData.postValue(getPreferenceBoolean(key))
            getKey(R.string.pref_key_cookie_mode_platform_displayable) ->
                displayablePlatformSet.postValue(
                    defaultSharedPreferences.getStringSet(key, setOf())
                )
            getKey(R.string.pref_key_additional_action_btn) ->
                showAdditionalActionButton.postValue(getPreferenceBoolean(key))
            getKey(R.string.pref_key_item_click_action) ->
                itemClickAction.postValue(getPreferenceString(key))
            getKey(R.string.pref_key_second_button_click_action) ->
                secondaryButtonClickAction.postValue(getPreferenceString(key))
            getKey(R.string.pref_key_group_mode_use_cookie) ->
                groupUseCookie.postValue(getPreferenceBoolean(key))
        }
    }
}
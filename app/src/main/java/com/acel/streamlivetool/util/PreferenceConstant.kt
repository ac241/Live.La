package com.acel.streamlivetool.util

import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication

object PreferenceConstant {
    val showAnchorImage
        get() =
            defaultSharedPreferences.getBoolean(
                MyApplication.application.resources.getString(R.string.pref_key_show_anchor_image),
                false
            )

    val showAnchorImageWhenMobileData
        get() =
            defaultSharedPreferences.getBoolean(
                MyApplication.application.resources.getString(R.string.pref_key_show_anchor_image_when_mobile_data),
                true
            )


    val fullVersion
        get() =
            defaultSharedPreferences.getBoolean(
                MyApplication.application.resources.getString(R.string.full_version),
                false
            )
    
}
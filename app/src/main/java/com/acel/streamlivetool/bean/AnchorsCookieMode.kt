package com.acel.streamlivetool.bean

import kotlinx.android.parcel.Parcelize

data class AnchorsCookieMode(
    val cookieOk: Boolean,
    val anchors: List<Anchor>?
) {
    class Anchor(
        val status: Boolean,
        val title: String
    ) : com.acel.streamlivetool.bean.Anchor()
}
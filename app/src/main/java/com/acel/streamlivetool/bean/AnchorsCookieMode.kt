package com.acel.streamlivetool.bean

data class AnchorsCookieMode(
    val cookieOk: Boolean,
    val anchors: List<Anchor>?,
    val message: String = ""
)
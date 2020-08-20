package com.acel.streamlivetool.platform.bean

import com.acel.streamlivetool.bean.Anchor

data class AnchorsCookieMode(
    val cookieOk: Boolean,
    val anchors: List<Anchor>?,
    val message: String = ""
)
package com.acel.streamlivetool.platform.bean

import com.acel.streamlivetool.bean.Anchor

data class ResultGetAnchorListByCookieMode(
    val isCookieValid: Boolean,
    val anchorList: List<Anchor>?,
    val message: String = ""
)
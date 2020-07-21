package com.acel.streamlivetool.bean

data class AnchorsCookieMode(
    val cookieOk: Boolean,
    val anchors: List<Anchor>?
) {
    class Anchor(
        val status: Boolean,
        val title: String,
        val avatar: String? = null,
        val keyFrame: String? = null
    ) : com.acel.streamlivetool.bean.Anchor()
}
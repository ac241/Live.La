package com.acel.streamlivetool.bean

class AnchorAttribute(
    val platform: String,
    val roomId: String,
    val isLive: Boolean,
    val title: String,
    val avatar: String? = null,
    val image: String? = null
) {

    fun getAnchorKey(): String {
        return platform + roomId
    }

}
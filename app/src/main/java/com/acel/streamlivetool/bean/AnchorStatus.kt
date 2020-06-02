package com.acel.streamlivetool.bean

class AnchorStatus(
    val platform: String,
    val roomId: String,
    val isLive: Boolean,
    val title: String
) {

    fun getAnchorKey(): String {
        return platform + roomId
    }

}
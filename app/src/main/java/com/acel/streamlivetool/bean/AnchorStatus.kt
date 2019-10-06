package com.acel.streamlivetool.bean

class AnchorStatus(platform: String, roomId: String, living: Boolean) {

    val platform: String
    val roomId: String
    val isLive: Boolean

    init {
        this.platform = platform
        this.roomId = roomId
        this.isLive = living
    }

    fun getAnchorKey(): String {
        return platform + roomId
    }

}
package com.acel.livela.bean

class AnchorStatus(platform: String, roomId: String, living: Boolean) {

    val platform: String
    val roomId: String
    val living: Boolean

    init {
        this.platform = platform
        this.roomId = roomId
        this.living = living
    }

    fun getAnchorKey(): String {
        return platform + roomId
    }

}
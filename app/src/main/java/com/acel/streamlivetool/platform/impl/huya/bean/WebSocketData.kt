package com.acel.streamlivetool.platform.impl.huya.bean

data class WebSocketData(
    val `data`: Data,
    val notice: String,
    val statusCode: Int,
    val statusMsg: String
)

data class Data(
    val badgeName: String,
    val content: String,
    val fansLevel: Int,
    val nobleLevel: Int,
    val roomId: Int,
    val sendNick: String,
    val senderAvatarUrl: String,
    val senderGender: Int,
    val senderLevel: Int,
    val showMode: Int,
    val unionId: String
)
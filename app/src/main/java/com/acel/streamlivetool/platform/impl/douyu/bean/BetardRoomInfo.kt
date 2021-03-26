package com.acel.streamlivetool.platform.impl.douyu.bean

data class BetardRoomInfo(
    val game: Game,
    val room: Room
)

data class Game(
    val tag_name: String
)

data class Room(
    val avatar: Avatar,
    val avatar_mid: String,
    val avatar_small: String,
    val nickname: String,
    val owner_uid: Int,
    val room_id: Int,
    val room_name: String,
    val room_pic: String,
    val room_src: String,
    val show_id: Int,
    val show_status: Int,
    val status: String,
    val videoLoop: Int
)

data class Avatar(
    val big: String,
    val middle: String,
    val small: String
)

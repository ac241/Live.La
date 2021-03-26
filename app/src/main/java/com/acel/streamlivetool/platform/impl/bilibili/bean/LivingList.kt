package com.acel.streamlivetool.platform.impl.bilibili.bean

data class LivingList(
    val code: Int,
    val `data`: Data,
    val message: String,
    val msg: String
) {

    data class Data(
        val count: Int,
        val rooms: List<Room>
    )

    data class Room(
        val area: Int,
        val area_name: String,
        val area_v2_id: Int,
        val area_v2_name: String,
        val area_v2_parent_id: Int,
        val area_v2_parent_name: String,
        val broadcast_type: Int,
        val cover_from_user: String,
        val face: String,
        val hidden_till: String,
        val keyframe: String,
        val link: String,
        val liveTime: Int,
        val live_status: Int,
        val live_time: Int,
        val lock_till: String,
        val nickname: String,
        val online: Int,
        val room_id: Int,
        val roomid: Int,
        val roomname: String,
        val short_id: Int,
        val tag_name: String,
        val tags: String,
        val title: String,
        val uid: Int,
        val uname: String
    )
}
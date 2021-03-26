package com.acel.streamlivetool.platform.impl.bilibili.bean

data class UnliveAnchor(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
) {

    data class Data(
        val has_more: Int,
        val no_room_count: Int,
        val rooms: List<Room>,
        val total_count: Int
    )

    data class Room(
        val announcement_content: String,
        val announcement_time: String,
        val area: Int,
        val area_name: String,
        val area_v2_id: Int,
        val area_v2_name: String,
        val area_v2_parent_id: Int,
        val area_v2_parent_name: String,
        val attentions: Int,
        val broadcast_type: Int,
        val bvid: String,
        val face: String,
        val link: String,
        val live_desc: String,
        val live_status: Int,
        val official_verify: Int,
        val p2p_type: Int,
        val recent_record_id: String,
        val recent_record_url_v2: String,
        val record_desc: String,
        val record_desc_v2: String,
        val roomid: Int,
        val special_attention: Int,
        val uid: Int,
        val uname: String
    )
}

package com.acel.streamlivetool.platform.bilibili.bean

data class LiveAnchor(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
) {

    data class Data(
        val big_card_type: Int,
        val card_type: Int,
        val rooms: List<Room>,
        val total_count: Int
    )

    data class Room(
        val accept_quality: List<Int>,
        val area: Int,
        val area_name: String,
        val area_v2_id: Int,
        val area_v2_name: String,
        val area_v2_parent_id: Int,
        val area_v2_parent_name: String,
        val broadcast_type: Int,
        val cover: String,
        val current_qn: Int,
        val current_quality: Int,
        val face: String,
        val flag: Int,
        val link: String,
        val live_tag_name: String,
        val live_time: Long,
        val official_verify: Int,
        val online: Int,
        val p2p_type: Int,
        val pendent_list: List<Any>,
        val pendent_ru: String,
        val pendent_ru_color: String,
        val pendent_ru_pic: String,
        val pk_id: Int,
        val play_url_card: String,
        val play_url_h265: String,
        val playurl: String,
        val quality_description: List<QualityDescription>,
        val roomid: Int,
        val special_attention: Int,
        val title: String,
        val uid: Int,
        val uname: String
    )

    data class QualityDescription(
        val desc: String,
        val qn: Int
    )
}
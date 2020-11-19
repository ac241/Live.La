package com.acel.streamlivetool.platform.bilibili.bean

data class RoomInfo(
    val code: Int,
    var `data`: Data?,
    val message: String,
    val msg: String
) {
    data class Data(
        val allow_change_area_time: Int,
        val allow_upload_cover_time: Int,
        val area_id: Int,
        val area_name: String,
        val area_pendants: String,
        val attention: Int,
        val background: String,
        val battle_id: Int,
        val description: String,
        val hot_words: List<String>,
        val hot_words_status: Int,
        val is_anchor: Int,
        val is_portrait: Boolean,
        val is_strict_room: Boolean,
        val keyframe: String,
        val live_status: Int,
        val live_time: String,
        val new_pendants: NewPendants,
        val old_area_id: Int,
        val online: Int,
        val parent_area_id: Int,
        val parent_area_name: String,
        val pendants: String,
        val pk_id: Int,
        val pk_status: Int,
        val room_id: Int,
        val room_silent_level: Int,
        val room_silent_second: Int,
        val room_silent_type: String,
        val short_id: Int,
        val studio_info: StudioInfo,
        val tags: String,
        val title: String,
        val uid: Long,
        val up_session: String,
        val user_cover: String,
        val verify: String
    )

    data class NewPendants(
        val badge: Badge,
        val frame: Frame,
        val mobile_badge: Any,
        val mobile_frame: MobileFrame
    )

    data class StudioInfo(
        val master_list: List<Any>,
        val status: Int
    )

    data class Badge(
        val desc: String,
        val name: String,
        val position: Int,
        val value: String
    )

    data class Frame(
        val area: Int,
        val area_old: Int,
        val bg_color: String,
        val bg_pic: String,
        val desc: String,
        val name: String,
        val position: Int,
        val use_old_area: Boolean,
        val value: String
    )

    data class MobileFrame(
        val area: Int,
        val area_old: Int,
        val bg_color: String,
        val bg_pic: String,
        val desc: String,
        val name: String,
        val position: Int,
        val use_old_area: Boolean,
        val value: String
    )
}
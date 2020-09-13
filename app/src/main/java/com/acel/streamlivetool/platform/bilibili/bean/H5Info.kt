package com.acel.streamlivetool.platform.bilibili.bean

import com.google.gson.annotations.SerializedName

data class H5Info(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
) {
    data class Data(
        val anchor_info: AnchorInfo,
        val banner_info: List<BannerInfo>,
        val new_switch_info: NewSwitchInfo,
        val room_info: RoomInfo
    )

    data class AnchorInfo(
        val base_info: BaseInfo,
        val relation_info: RelationInfo
    )

    data class BannerInfo(
        val img: String,
        val link: String
    )

    data class NewSwitchInfo(
        @SerializedName("room-info-popularity")
        val room_info_popularity: Int,
        @SerializedName("room-player-watermark")
        val room_player_watermark: Int,
        @SerializedName("room-recommend-live_off")
        val room_recommend_live_off: Int,
        @SerializedName("room-tab")
        val room_tab: Int
    )

    data class RoomInfo(
        val area_id: Int,
        val area_name: String,
        val cover: String,
        val description: String,
        val live_start_time: Int,
        val live_status: Int,
        val online: Int,
        val parent_area_id: Int,
        val parent_area_name: String,
        val room_id: Int,
        val title: String,
        val uid: Int
    )

    data class BaseInfo(
        val face: String,
        val uname: String
    )

    data class RelationInfo(
        val attention: Int
    )
}


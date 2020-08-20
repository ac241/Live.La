package com.acel.streamlivetool.platform.douyu.bean

data class Followed(
    val `data`: Data,
    val error: Int,
    val msg: String
) {
    data class Data(
        val allColumn: List<AllColumn>,
        val has_special: Int,
        val limit: Int,
        val list: List<AnchorX>,
        val nowPage: Int,
        val pageCount: Int,
        val total: Int
    )

    data class AllColumn(
        val cate_id: Int,
        val cate_name: String,
        val is_audio: Int,
        val is_show_rank_list: Int,
        val push_vertical_screen: Int,
        val short_name: String
    )

    data class AnchorX(
        val avatar_small: String,
        val cate_id: Int,
        val child_id: Int,
        val close_notice: String,
        val close_notice_ctime: String,
        val game_name: String,
        val hasvid: Int,
        val icon_outing: Int,
        val isVertical: Int,
        val is_special: Int,
        val jumpUrl: String,
        val nickname: String,
        val nrt: Int,
        val online: String,
        val rmf3: Int,
        val room_id: Int,
        val room_name: String,
        val room_src: String,
        val rpos: Int,
        val show_status: Int,
        val show_time: Int,
        val status: Int,
        val sub_rt: Int,
        val url: String,
        val vertical_src: String,
        val videoLoop: Int,
        val vurl: String
    )

}

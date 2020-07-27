package com.acel.streamlivetool.platform.huomao.bean

data class UsersSubscribe(
    val code: String,
    val `data`: Data,
    val message: String,
    val num: Int,
    val status: Boolean
) {
    data class Data(
        val totalCount: Int,
        val usersSubChannels: List<UsersSubChannel>
    )

    data class UsersSubChannel(
        val advance: String,
        val channel: String,
        val event_endtime: String,
        val event_starttime: String,
        val gameCname: String,
        val gameEname: String,
        val game_logo: String,
        val game_url_rule: String,
        val gid: Any,
        val headimg: Headimg,
        val id: String,
        val image: String,
        val is_auto_vod: String,
        val is_conmic: String,
        val is_del: String,
        val is_event: String,
        val is_live: Int,
        val is_pk: String,
        val is_video: Int,
        val list_a_n_color: String,
        val nickname: String,
        val room_number: String,
        val stream: String,
        val streamNoEncode: String,
        val uid: String,
        val user_lv: Int,
        val username: String,
        val views: String
    )

    data class Headimg(
        val big: String,
        val normal: String,
        val origin: String,
        val small: String
    )

}
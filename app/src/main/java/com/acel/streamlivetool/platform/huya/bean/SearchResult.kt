package com.acel.streamlivetool.platform.huya.bean

data class SearchResult(
    val response: Response,
    val responseHeader: ResponseHeader
) {
    data class Response(
        val `1`: X1,
        val `1024`: X1024,
        val `1026`: List<Any>,
        val `1027`: List<Any>,
        val `122`: X122,
        val `20151`: X20151,
        val `3`: X3,
        val `6324`: X6324
    )

    data class ResponseHeader(
        val QTime: Int,
        val status: Int
    )

    data class X1(
        val docs: List<Doc>,
        val numFound: Int,
        val start: Int
    )

    data class X1024(
        val docs: List<Any>,
        val numFound: Int,
        val start: Int
    )

    data class X122(
        val docs: List<DocX>,
        val numFound: Int,
        val start: Int
    )

    data class X20151(
        val docs: List<Any>,
        val numFound: Int,
        val start: Int
    )

    data class X3(
        val docs: List<Any>,
        val numFound: Int,
        val start: Int
    )

    data class X6324(
        val docs: List<Any>,
        val numFound: Int,
        val start: Int
    )

    data class Doc(
        val aid: Int,
        val gameLiveOn: Boolean,
        val game_activityCount: Int,
        val game_avatarUrl180: String,
        val game_avatarUrl52: String,
        val game_channel: Long,
        val game_id: Int,
        val game_level: Int,
        val game_liveLink: String,
        val game_longChannel: Int,
        val game_name: String,
        val game_nick: String,
        val game_profileLink: String,
        val game_recommendStatus: Int,
        val game_subChannel: Long,
        val live_intro: String,
        val rec_game_name: String,
        val rec_live_time: Int,
        val recommended_text: String,
        val room_id: Int,
        val sTagName: String,
        val screen_type: Int,
        val uid: Long,
        val yyid: Long
    )

    data class DocX(
        val cover: String,
        val play_sum: Int,
        val title: String,
        val uptime: String,
        val url: String,
        val user_avatar: String,
        val user_id: Long,
        val user_nickname: String,
        val vid: Int
    )
}
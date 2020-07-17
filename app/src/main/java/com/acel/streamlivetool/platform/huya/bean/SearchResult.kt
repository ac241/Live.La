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
        val docs: List<Any>,
        val numFound: Int,
        val start: Int
    )

    data class X20151(
        val docs: List<Any>,
        val numFound: Int,
        val start: Int
    )

    data class X3(
        val docs: List<DocX>,
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
        val game_channel: Int,
        val game_id: Int,
        val game_level: Int,
        val game_liveLink: String,
        val game_longChannel: Int,
        val game_name: String,
        val game_nick: String,
        val game_profileLink: String,
        val game_recommendStatus: Int,
        val game_subChannel: Int,
        val live_intro: String,
        val rec_game_name: String,
        val rec_live_time: Int,
        val recommended_text: String,
        val room_id: Int,
        val sTagName: String,
        val screen_type: Int,
        val uid: Int,
        val yyid: Int
    )

    data class DocX(
        val aid: Int,
        val gameId: Int,
        val gameName: String,
        val game_channel: Int,
        val game_imgUrl: String,
        val game_introduction: String,
        val game_nick: String,
        val game_privateHost: String,
        val game_roomName: String,
        val game_screenshot: String,
        val game_shortChannel: Int,
        val game_subChannel: Int,
        val game_total_count: Int,
        val liveSourceType: String,
        val room_id: Int,
        val screen_type: Int,
        val tag_name: String,
        val uid: Int,
        val yyid: Int
    )
}

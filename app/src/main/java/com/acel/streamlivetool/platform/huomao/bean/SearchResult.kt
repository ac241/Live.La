package com.acel.streamlivetool.platform.huomao.bean

data class SearchResult(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val anchor: Anchor,
        val channel: Channel,
        val event: Event,
        val event_new: List<Any>,
        val exact: Exact,
        val game: Game,
        val mobileGame: MobileGame,
        val news: News,
        val oneChannel: OneChannelXXXXX,
        val search_words: String
    )

    data class Anchor(
        val count: String,
        val list: List<AnchorX>,
        val oneChannel: OneChannel,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    data class Channel(
        val count: Int,
        val list: List<X>,
        val oneChannel: OneChannelX,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    data class Event(
        val count: Int,
        val list: List<Any>,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    data class Exact(
        val count: Int,
        val list: List<Any>,
        val oneChannel: OneChannelXX,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    data class Game(
        val count: Int,
        val list: List<Any>,
        val oneChannel: OneChannelXXX,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    data class MobileGame(
        val count: Int,
        val list: List<Any>,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    data class News(
        val count: Int,
        val list: List<Any>,
        val oneChannel: OneChannelXXXX,
        val page: Int,
        val page_list: Int,
        val total_page: Int
    )

    class OneChannelXXXXX(
    )

    data class AnchorX(
        val audience: String,
        val audience_int: Double,
        val channel: String,
        val cid: String,
        val img: Img,
        val is_live: Any,
        val nickname: String,
        val room_number: String,
        val screenType: Int,
        val search_weight: String,
        val type: Int,
        val views: String,
        val views_int: Int
    )

    class OneChannel(
    )

    data class Img(
        val big: String,
        val normal: String,
        val origin: String,
        val small: String
    )

    data class X(
        val channel: String,
        val event_endtime: String,
        val event_starttime: String,
        val gameCname: String,
        val gameEname: String,
        val game_logo: String,
        val game_url_rule: String,
        val gid: String,
        val headimg: Headimg,
        val id: String,
        val image: String,
        val is_conmic: String,
        val is_del: String,
        val is_event: String,
        val is_live: Any,
        val is_pk: String,
        val list_a_n_color: String,
        val live_last_start_time: String,
        val nickname: String,
        val room_number: String,
        val screenType: Int,
        val sh_zt: String,
        val status: String,
        val stream: String,
        val streamNoEncode: String,
        val tj_pic: String,
        val type: Int,
        val uid: String,
        val user_lv: Int,
        val username: String,
        val views: String
    )

    class OneChannelX(
    )

    data class Headimg(
        val big: String,
        val normal: String,
        val origin: String,
        val small: String
    )

    class OneChannelXX(
    )

    class OneChannelXXX(
    )

    class OneChannelXXXX(
    )
}
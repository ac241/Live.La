package com.acel.streamlivetool.platform.huomao.bean

data class SearchResult(
    val code: Int,
    val `data`: Data,
    val message: String,
    val status: Boolean
) {
    data class Data(
        val anchor: Anchor
    )

    data class Anchor(
        val count: String,
        val list: List<AnchorX>,
        val page: Int,
        val page_list: Int,
        val total_page: Int
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

    data class Img(
        val big: String,
        val normal: String,
        val origin: String,
        val small: String
    )

}
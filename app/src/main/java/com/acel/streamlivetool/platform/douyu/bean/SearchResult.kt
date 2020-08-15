package com.acel.streamlivetool.platform.douyu.bean

data class SearchResult(
    val `data`: Data,
    val error: Int,
    val msg: String
) {

    data class Data(
        val cateResult: List<Any>,
        val roomResult: List<RoomResult>
    )

    data class RoomResult(
        val algorithm: Algorithm,
        val avatar: String,
        val cateName: String,
        val cid: Int,
        val isLive: Int,
        val kw: String,
        val nickName: String,
        val rid: Int,
        val vipId: Int
    )

    data class Algorithm(
        val rt: String
    )
}
package com.acel.streamlivetool.platform.huya.bean

data class Subscribe(
    val result: Result,
    val status: Long
) {
    data class Result(
        val list: List<Anchor>,
        val liveCount: Long,
        val page: Long,
        val pageSize: Long,
        val totalCount: Long,
        val totalPage: Long
    )

    data class Anchor(
        val activityCount: Long,
        val activityId: Long,
        val avatar180: String,
        val channel: Long,
        val gameId: Long,
        val gameName: String,
        val iRelation: Long,
        val intro: String,
        val isLive: Boolean,
        val nick: String,
        val privateHost: String,
        val profileRoom: Long,
        val screenshot: String,
        val startTime: Long,
        val subChannel: Long,
        val totalCount: Long,
        val uid: Long,
        val yyid: Long
    )
}
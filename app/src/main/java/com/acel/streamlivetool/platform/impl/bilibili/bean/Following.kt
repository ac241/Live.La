package com.acel.streamlivetool.platform.impl.bilibili.bean

data class Following(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
) {
    data class Data(
        val count: Int,
        val list: List<Anchor>,
        val pageSize: Int,
        val title: String,
        val totalPage: Int
    ) {
        data class Anchor(
            val area_name: String,
            val area_value: String,
            val clipnum: Int,
            val face: String,
            val fans_num: Int,
            val is_attention: Int,
            val live_status: Int,
            val recent_record_id: String,
            val record_num: Int,
            val roomid: Int,
            val tags: String,
            val title: String,
            val uid: Int,
            val uname: String
        )
    }


}


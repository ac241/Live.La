package com.acel.streamlivetool.platform.impl.bilibili.bean

data class DanmuInfo(
    val code: Int,
    val `data`: Data,
    val message: String,
    val ttl: Int
)

data class Data(
    val business_id: Int,
    val group: String,
    val host_list: List<Host>,
    val max_delay: Int,
    val refresh_rate: Int,
    val refresh_row_factor: Double,
    val token: String
)

data class Host(
    val host: String,
    val port: Int,
    val ws_port: Int,
    val wss_port: Int
)
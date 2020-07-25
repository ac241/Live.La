package com.acel.streamlivetool.platform.egameqq.bean

data class PlayerInfo(
    val anchorId: Int,
    val clarityLimit: Int,
    val coverpic: String,
    val enableP2pGrey: Boolean,
    val enableQuic: Boolean,
    val enableRtcGrey: Boolean,
    val enableVp9Grey: Boolean,
    val flash: Boolean,
    val hls: Boolean,
    val modId: String,
    val p2p: Boolean,
    val playerTypesOrder: List<String>,
    val provider: Int,
    val roomId: String,
    val rtcLive: Boolean,
    val urlArray: List<UrlArray>,
    val vid: String,
    val videoAttr: VideoAttr,
    val videoType: String,
    val vp9: Boolean
){
    data class UrlArray(
        val bitrate: Any,
        val desc: String,
        val levelType: Int,
        val playUrl: String,
        val vp9PlayUrl: String
    )

    data class VideoAttr(
        val pid: String,
        val source: String
    )
}


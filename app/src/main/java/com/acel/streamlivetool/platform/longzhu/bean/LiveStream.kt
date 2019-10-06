package com.acel.streamlivetool.platform.longzhu.bean
import com.google.gson.annotations.SerializedName


data class LiveStream(
    @SerializedName("defaultLine")
    val defaultLine: Int,
    @SerializedName("defaultRateLevel")
    val defaultRateLevel: Int,
    @SerializedName("inbandwidth")
    val inbandwidth: Double,
    @SerializedName("isPtoP")
    val isPtoP: Boolean,
    @SerializedName("isTransfer")
    val isTransfer: Int,
    @SerializedName("liveSourceType")
    val liveSourceType: Int,
    @SerializedName("liveUrl")
    val liveUrl: String,
    @SerializedName("p2pType")
    val p2pType: Int,
    @SerializedName("pkPlayLines")
    val pkPlayLines: List<Any>,
    @SerializedName("playLines")
    val playLines: List<PlayLine>,
    @SerializedName("pushLiveStreamType")
    val pushLiveStreamType: Int
)

data class PlayLine(
    @SerializedName("lineType")
    val lineType: Int,
    @SerializedName("playLiveStreamType")
    val playLiveStreamType: Int,
    @SerializedName("urls")
    val urls: List<Url>
)

data class Url(
    @SerializedName("description")
    val description: String,
    @SerializedName("ext")
    val ext: String,
    @SerializedName("rateLevel")
    val rateLevel: Int,
    @SerializedName("resolution")
    val resolution: String,
    @SerializedName("securityUrl")
    val securityUrl: String,
    @SerializedName("timeMove")
    val timeMove: Boolean
)
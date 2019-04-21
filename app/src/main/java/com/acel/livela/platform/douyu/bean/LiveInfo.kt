package com.acel.livela.platform.douyu.bean

import com.google.gson.annotations.SerializedName


data class LiveInfo(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("error")
    val error: Int,
    @SerializedName("msg")
    val msg: String
) {
    data class Data(
        @SerializedName("cdnsWithName")
        val cdnsWithName: List<CdnsWithName>,
        @SerializedName("client_ip")
        val clientIp: String,
        @SerializedName("eticket")
        val eticket: Any,
        @SerializedName("inNA")
        val inNA: Int,
        @SerializedName("isPassPlayer")
        val isPassPlayer: Int,
        @SerializedName("is_mixed")
        val isMixed: Boolean,
        @SerializedName("mixedCDN")
        val mixedCDN: String,
        @SerializedName("mixed_live")
        val mixedLive: String,
        @SerializedName("mixed_url")
        val mixedUrl: String,
        @SerializedName("multirates")
        val multirates: List<Multirate>,
        @SerializedName("online")
        val online: Int,
        @SerializedName("p2p")
        val p2p: Int,
        @SerializedName("rate")
        val rate: Int,
        @SerializedName("rateSwitch")
        val rateSwitch: Int,
        @SerializedName("room_id")
        val roomId: Int,
        @SerializedName("rtmp_cdn")
        val rtmpCdn: String,
        @SerializedName("rtmp_live")
        val rtmpLive: String,
        @SerializedName("rtmp_url")
        val rtmpUrl: String,
        @SerializedName("streamStatus")
        val streamStatus: Int
    ) {
        data class Multirate(
            @SerializedName("highBit")
            val highBit: Int,
            @SerializedName("name")
            val name: String,
            @SerializedName("rate")
            val rate: Int
        )

        data class CdnsWithName(
            @SerializedName("cdn")
            val cdn: String,
            @SerializedName("name")
            val name: String
        )
    }
}


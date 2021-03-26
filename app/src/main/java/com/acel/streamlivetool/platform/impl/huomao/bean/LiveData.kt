package com.acel.streamlivetool.platform.impl.huomao.bean

import com.google.gson.annotations.SerializedName


data class LiveData(
    @SerializedName("by")
    val `by`: String,
    @SerializedName("cid")
    val cid: String,
    @SerializedName("clarity_openstat")
    val clarityOpenstat: String,
    @SerializedName("defaultBitRate")
    val defaultBitRate: String,
    @SerializedName("is_BlueRay")
    val isBlueRay: String,
    @SerializedName("is_videoPls")
    val isVideoPls: Boolean,
    @SerializedName("is_zhuanma")
    val isZhuanma: String,
    @SerializedName("roomStatus")
    val roomStatus: String,
    @SerializedName("screenType")
    val screenType: Int,
    @SerializedName("streamList")
    val streamList: List<Stream>,
    @SerializedName("streamType")
    val streamType: Int,
    @SerializedName("time")
    val time: Int
) {
    data class Stream(
        @SerializedName("default")
        val default: Int,
        @SerializedName("id")
        val id: String,
        @SerializedName("list")
        val list: List<X>,
        @SerializedName("list_hls")
        val listHls: List<Hls>
    ) {
        data class Hls(
            @SerializedName("type")
            val type: String,
            @SerializedName("url")
            val url: String
        )

        data class X(
            @SerializedName("type")
            val type: String,
            @SerializedName("url")
            val url: String
        )
    }
}



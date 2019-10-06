package com.acel.streamlivetool.platform.bilibili.bean

import com.google.gson.annotations.SerializedName


data class PlayUrl(
    @SerializedName("code")
    val code: Int,
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("message")
    val message: String,
    @SerializedName("msg")
    val msg: String
) {
    data class Data(
        @SerializedName("accept_quality")
        val acceptQuality: List<String>,
        @SerializedName("current_qn")
        val currentQn: Int,
        @SerializedName("current_quality")
        val currentQuality: Int,
        @SerializedName("durl")
        val durl: List<Durl>,
        @SerializedName("quality_description")
        val qualityDescription: List<QualityDescription>
    ) {
        data class Durl(
            @SerializedName("length")
            val length: Int,
            @SerializedName("order")
            val order: Int,
            @SerializedName("stream_type")
            val streamType: Int,
            @SerializedName("url")
            val url: String
        )

        data class QualityDescription(
            @SerializedName("desc")
            val desc: String,
            @SerializedName("qn")
            val qn: Int
        )
    }
}





package com.acel.livela.platform.bilibili.bean

import com.google.gson.annotations.SerializedName


data class StaticRoomInfo(
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
        @SerializedName("area_name")
        val areaName: String,
        @SerializedName("area_v2_id")
        val areaV2Id: String,
        @SerializedName("face")
        val face: String,
        @SerializedName("live_status")
        val liveStatus: Int,
        @SerializedName("live_time")
        val liveTime: String,
        @SerializedName("on_flag")
        val onFlag: String,
        @SerializedName("online")
        val online: Int,
        @SerializedName("roomid")
        val roomid: String,
        @SerializedName("round_status")
        val roundStatus: String,
        @SerializedName("title")
        val title: String,
        @SerializedName("uid")
        val uid: Int,
        @SerializedName("uname")
        val uname: String,
        @SerializedName("user_cover")
        val userCover: String
    )
}


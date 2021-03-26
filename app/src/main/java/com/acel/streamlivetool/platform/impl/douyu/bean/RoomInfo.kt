package com.acel.streamlivetool.platform.impl.douyu.bean
import com.google.gson.annotations.SerializedName


data class RoomInfo(
    @SerializedName("data")
    val `data`: Data,
    @SerializedName("error")
    val error: Int
)

data class Data(
    @SerializedName("avatar")
    val avatar: String,
    @SerializedName("cate_id")
    val cateId: String,
    @SerializedName("cate_name")
    val cateName: String,
    @SerializedName("fans_num")
    val fansNum: String,
    @SerializedName("gift")
    val gift: List<Gift>,
    @SerializedName("hn")
    val hn: Int,
    @SerializedName("online")
    val online: Int,
    @SerializedName("owner_name")
    val ownerName: String,
    @SerializedName("owner_weight")
    val ownerWeight: String,
    @SerializedName("room_id")
    val roomId: String,
    @SerializedName("room_name")
    val roomName: String,
    @SerializedName("room_status")
    val roomStatus: String,
    @SerializedName("room_thumb")
    val roomThumb: String,
    @SerializedName("start_time")
    val startTime: String
)

data class Gift(
    @SerializedName("desc")
    val desc: String,
    @SerializedName("gx")
    val gx: Int,
    @SerializedName("himg")
    val himg: String,
    @SerializedName("id")
    val id: String,
    @SerializedName("intro")
    val intro: String,
    @SerializedName("mimg")
    val mimg: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("pc")
    val pc: Double,
    @SerializedName("type")
    val type: String
)
package com.acel.streamlivetool.platform.douyu.bean
import com.google.gson.annotations.SerializedName


data class RoomInfoMsg(
    @SerializedName("error")
    val error: Int
)
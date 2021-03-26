package com.acel.streamlivetool.platform.impl.douyu.bean
import com.google.gson.annotations.SerializedName


data class RoomInfoMsg(
    @SerializedName("error")
    val error: Int
)
package com.acel.livela.platform.douyu.bean

import com.google.gson.annotations.SerializedName


data class H5Enc(
    @SerializedName("data")
    val `data`: HashMap<String, String>,
    @SerializedName("error")
    val error: Int
)

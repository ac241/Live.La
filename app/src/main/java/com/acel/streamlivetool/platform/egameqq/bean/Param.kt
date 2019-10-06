package com.acel.streamlivetool.platform.egameqq.bean

import com.google.gson.annotations.SerializedName


data class Param(
    @SerializedName("key")
    val key: Key
) {

    data class Key(
        @SerializedName("method")
        val method: String = "get_anchor_card_info",
        @SerializedName("module")
        val module: String = "pgg_anchor_card_svr",
        @SerializedName("param")
        val `param`: ParamX
    ) {
        data class ParamX(
            @SerializedName("anchor_uid")
            val anchorUid: Int,
            @SerializedName("user_uid")
            val userUid: Int = 0
        )
    }


}
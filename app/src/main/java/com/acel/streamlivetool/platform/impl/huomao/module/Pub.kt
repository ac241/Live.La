package com.acel.streamlivetool.platform.impl.huomao.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.impl.huomao.HuomaoImpl
import com.acel.streamlivetool.platform.impl.huomao.bean.RoomInfo
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson

object Pub {
    fun getRoomInfo(queryAnchor: Anchor): RoomInfo? {
        val html = HuomaoImpl.huomaoService.getRoomInfo(queryAnchor.showId).execute().body()
        val channelOneInfo = html?.let {
            val temp = TextUtil.subString(it, "channelOneInfo = ", "};")
            if (temp == null) null else "$temp}"
        }
        return if (channelOneInfo != null)
            Gson().fromJson(channelOneInfo, RoomInfo::class.java)
        else
            null
    }
}
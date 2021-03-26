package com.acel.streamlivetool.platform.impl.huya.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.impl.huya.HuyaImpl.Companion.huyaService

object Pub {
    fun getMHtml(queryAnchor: Anchor): String? {
        return huyaService.getMHtml(queryAnchor.showId).execute().body()
    }
}
package com.acel.streamlivetool.platform.impl.huya.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.base.StreamingLiveModule
import com.acel.streamlivetool.platform.impl.huya.module.Pub.getMHtml
import com.acel.streamlivetool.util.TextUtil

object HuyaStreamingLiveModule : StreamingLiveModule {
    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
        val html = getMHtml(queryAnchor)
        html?.let {
            val streamStr = TextUtil.subString(it, "liveLineUrl = \"", "\";")
            if (streamStr != null)
                return StreamingLive(url = "https:$streamStr", null, null)
        }
        return null
    }
}
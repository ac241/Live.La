package com.acel.streamlivetool.platform.base

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive

interface IStreamingLive {
    /**
     * 获取直播流
     * @param queryQuality 质量描述 @ [StreamingLive]
     */
    fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality? = null
    ): StreamingLive?
}
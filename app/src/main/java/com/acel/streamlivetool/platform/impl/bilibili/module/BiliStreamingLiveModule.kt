package com.acel.streamlivetool.platform.impl.bilibili.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.base.StreamingLiveModule
import com.acel.streamlivetool.platform.impl.bilibili.BilibiliImpl
import com.acel.streamlivetool.platform.impl.bilibili.bean.RoomPlayInfo

object BiliStreamingLiveModule : StreamingLiveModule {
    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
        //return 开头的为返回数据
        //info 开头的为获取到的数据
        val roomPlayInfo =
            BilibiliImpl.bilibiliService.getRoomPlayInfo(
                queryAnchor.roomId,
                queryQuality?.num ?: 10000
            )
                .execute()
                .body()
        roomPlayInfo ?: return null
        if (roomPlayInfo.code != 0)
            return null

        val infoQualityList = roomPlayInfo.data.play_url.quality_description
        val returnQualityList = mutableListOf<StreamingLive.Quality>()
        infoQualityList.forEach {
            returnQualityList.add(StreamingLive.Quality(it.desc, it.qn))
        }
        //info 当前的质量
        val infoCurrentQuality = roomPlayInfo.data.play_url.current_qn
        val infoCurrentQualityDescription = infoQualityList[infoQualityList.indexOf(
            RoomPlayInfo.QualityDescription(
                "",
                infoCurrentQuality
            )
        )]

        //返回数据
        val returnUrl: String = roomPlayInfo.data.play_url.durl[0].url
        val returnCurrentQuality = StreamingLive.Quality(
            infoCurrentQualityDescription.desc,
            infoCurrentQualityDescription.qn
        )

        return StreamingLive(
            url = returnUrl,
            currentQuality = returnCurrentQuality,
            qualityList = returnQualityList
        )
    }
}
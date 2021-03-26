package com.acel.streamlivetool.platform.impl.egameqq.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.base.IStreamingLive
import com.acel.streamlivetool.platform.impl.egameqq.EgameqqImpl.Companion.egameqqService

object EgameqqStreamingLiveModule : IStreamingLive {

    override fun getStreamingLive(queryAnchor: Anchor, queryQuality: StreamingLive.Quality?): StreamingLive? {
        val liveAndProfileInfo =
            egameqqService.getLiveAndProfileInfo(liveAndProfileInfoParam(queryAnchor.showId))
                .execute().body()
        liveAndProfileInfo?.data?.key?.retBody?.data?.let { data ->
            val streamInfo = data.video_info.stream_infos[0]
            return StreamingLive(url = streamInfo.play_url, null, null)
        }
        return null
    }

    private fun liveAndProfileInfoParam(roomId: String) =
        "{\"key\":{\"module\":\"pgg_live_read_svr\",\"method\":\"get_live_and_profile_info\",\"param\":{\"anchor_id\":${roomId},\"layout_id\":\"\",\"index\":0}}}"


}
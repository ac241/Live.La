package com.acel.streamlivetool.platform.impl.egameqq.module

import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.base.AnchorModule
import com.acel.streamlivetool.platform.impl.egameqq.EgameqqImpl.Companion.egameqqService
import com.acel.streamlivetool.util.TimeUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup

class EgameqqAnchorModule(private val platform: String) : AnchorModule {
    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val liveAndProfileInfo =
            egameqqService.getLiveAndProfileInfo(liveAndProfileInfoParam(queryAnchor.showId))
                .execute().body()
        if (liveAndProfileInfo == null || liveAndProfileInfo.ecode != 0)
            return null
        else {
            if (liveAndProfileInfo.data.key.retCode != 0) {
                return null
            } else {
                liveAndProfileInfo.data.key.retBody.data.apply {
                    return Anchor(
                        platform = platform,
                        nickname = profile_info.nick_name,
                        showId = profile_info.uid.toString(),
                        roomId = profile_info.uid.toString(),
                        status = profile_info.is_live == 1,
                        title = video_info.title,
                        avatar = profile_info.face_url,
                        keyFrame = video_info.url.trim(),
                        typeName = video_info.appname,
                        liveTime = TimeUtil.timestampToString(video_info.start_tm)
                    )
                }
            }
        }
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val liveAndProfileInfo =
            egameqqService.getLiveAndProfileInfo(liveAndProfileInfoParam(queryAnchor.showId))
                .execute().body()
        liveAndProfileInfo?.data?.key?.retBody?.data?.let { data ->
            queryAnchor.apply {
                status = data.profile_info.is_live == 1
                title = data.video_info.title
                avatar = data.profile_info.face_url
                keyFrame = data.video_info.url.trim()
                typeName = data.video_info.appname
                liveTime = TimeUtil.timestampToString(data.video_info.start_tm)
            }
            return true
        }
        return false
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val html = egameqqService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        if (html != null)
            html.apply {
                val document = Jsoup.parse(this)
                val anchors = document.getElementsByClass("gui-list-anchor")
                runBlocking {
                    anchors.forEachIndexed { index, element ->
                        if (index >= 10)
                            return@runBlocking
                        async(Dispatchers.IO) {
                            val id = element.getElementsByTag("a").attr("href").replace("/", "")
                            getAnchor(id)?.let { list.add(it) }
                        }.start()
                    }
                }
                return list
            }
        else
            return list
    }


    private fun liveAndProfileInfoParam(roomId: String) =
        "{\"key\":{\"module\":\"pgg_live_read_svr\",\"method\":\"get_live_and_profile_info\",\"param\":{\"anchor_id\":${roomId},\"layout_id\":\"\",\"index\":0}}}"

    private fun getAnchor(roomId: String): Anchor? {
        return getAnchor(Anchor(platform, "", roomId, roomId))
    }
}

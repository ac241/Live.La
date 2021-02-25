package com.acel.streamlivetool.platform.egameqq

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.platform.egameqq.bean.EgameQQAnchor
import com.acel.streamlivetool.platform.egameqq.bean.Param
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.TimeUtil
import com.acel.streamlivetool.util.TimeUtil.timestampToString
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.jsoup.Jsoup


class EgameqqImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            EgameqqImpl()
        }
    }

    override val platform: String = "egameqq"
    override val platformShowNameRes: Int = R.string.egameqq
    override val iconRes: Int = R.drawable.ic_egameqq
    override val supportCookieMode: Boolean = true
    private val egameqqService: EgameqqApi = retrofit.create(EgameqqApi::class.java)

    private fun getHtml(queryAnchor: Anchor): String? {
        return egameqqService.getHtml(queryAnchor.showId).execute().body()
    }

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val liveAndProfileInfo =
                egameqqService.getLiveAndProfileInfo(liveAndProfileInfoParam(queryAnchor.showId))
                        .execute().body()
        if (liveAndProfileInfo == null || liveAndProfileInfo.ecode != 0)
            return null
        else {
            if (liveAndProfileInfo.data.key.retCode != 0) {
                Log.d("acel_log#getAnchor", liveAndProfileInfo.data.key.retMsg)
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
                            liveTime = timestampToString(video_info.start_tm)
                    )
                }
            }
        }
    }

    private fun liveAndProfileInfoParam(roomId: String) =
            "{\"key\":{\"module\":\"pgg_live_read_svr\",\"method\":\"get_live_and_profile_info\",\"param\":{\"anchor_id\":${roomId},\"layout_id\":\"\",\"index\":0}}}"

    private fun getEgameAnchor(roomId: String): EgameQQAnchor? {
        val param =
                Param(Param.Key(param = Param.Key.ParamX(anchorUid = roomId.toInt())))
        return egameqqService.getAnchor(Gson().toJson(param)).execute().body()
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
                liveTime = timestampToString(data.video_info.start_tm)
            }
            return true
        }
        return false
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true

    override fun getStreamingLive(queryAnchor: Anchor, queryQualityDesc: StreamingLive.QualityDescription?): StreamingLive? {
        val liveAndProfileInfo =
                egameqqService.getLiveAndProfileInfo(liveAndProfileInfoParam(queryAnchor.showId))
                        .execute().body()
        liveAndProfileInfo?.data?.key?.retBody?.data?.let { data ->
            val streamInfo = data.video_info.stream_infos[0]
            return StreamingLive(url = streamInfo.play_url, null, null)
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri =
                Uri.parse(
                        "qgameapi://video/room?aid=${anchor.roomId}"
                )
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
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

    private fun getAnchor(roomId: String): Anchor? {
        return getAnchor(Anchor(platform, "", roomId, roomId))
    }

    override fun getAnchorsWithCookieMode(): ResultGetAnchorListByCookieMode {
        if (getCookie().isEmpty())
            return super.getAnchorsWithCookieMode()
        val list = egameqqService.getFollowList(getCookie()).execute().body()
        if (list?.ecode != 0)
            return ResultGetAnchorListByCookieMode(
                    success = false,
                    isCookieValid = false,
                    anchorList = null,
                    message = "ecode 0"
            )
        if (list.uid == 0)
            return ResultGetAnchorListByCookieMode(
                    success = false,
                    isCookieValid = false,
                    anchorList = null,
                    message = "cookie invalid"
            )
        if (list.data.key.retCode != 0)
            return ResultGetAnchorListByCookieMode(
                    success = false,
                    isCookieValid = false,
                    anchorList = null,
                    message = list.data.key.retMsg
            )
        else {
            val anchorList = mutableListOf<Anchor>()
            with(list.data.key.retBody.data.online_follow_list) {
                this.forEach {
                    anchorList.add(
                            Anchor(
                                    platform = platform,
                                    nickname = it.live_info.anchor_name,
                                    showId = it.live_info.anchor_id.toString(),
                                    roomId = it.live_info.anchor_id.toString(),
                                    status = it.status == 1,
                                    title = it.live_info.title,
                                    avatar = it.live_info.anchor_face_url,
                                    keyFrame = it.live_info.video_info.url,
                                    typeName = it.live_info.appname,
                                    online = AnchorUtil.formatOnlineNumber(it.live_info.online),
                                    liveTime = timestampToString(it.last_play_time)
                            )
                    )
                }
            }
            return ResultGetAnchorListByCookieMode(
                    success = true,
                    isCookieValid = true,
                    anchorList = anchorList
            )
        }
    }

    override fun getLoginUrl(): String {
        return "https://egame.qq.com/usercenter/followlist"
    }

    override fun checkLoginOk(cookie: String): Boolean {
        return cookie.contains("pgg_uid") && cookie.contains("pgg_access_token")
    }

    override fun loginWithPcAgent(): Boolean {
        return true
    }
}
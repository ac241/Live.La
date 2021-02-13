package com.acel.streamlivetool.platform.egameqq

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.platform.egameqq.bean.EgameQQAnchor
import com.acel.streamlivetool.platform.egameqq.bean.Param
import com.acel.streamlivetool.platform.egameqq.bean.PlayerInfo
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.TimeUtil
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
        val html = getHtml(queryAnchor)
        html?.let {
            val tempRoomId = TextUtil.subString(html, "channelId:\"", "\",")
            tempRoomId?.apply {
                val roomId = if (contains("_")) split("_")[1] else this
                queryAnchor.roomId = roomId
                val anchor = getEgameAnchor(queryAnchor)
                anchor?.let {
                    it.data.key.retBody.data.apply {
                        return Anchor(
                            platform = platform,
                            nickname = nickName,
                            showId = aliasId.toString(),
                            roomId = uid.toString(),
                            status = isLive == 1,
                            avatar = faceUrl
                        )
                    }
                }
            }
        }
        return null
    }

    private fun getEgameAnchor(queryAnchor: Anchor): EgameQQAnchor? {
        val param =
            Param(Param.Key(param = Param.Key.ParamX(anchorUid = queryAnchor.roomId.toInt())))
        return egameqqService.getAnchor(Gson().toJson(param)).execute().body()
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val anchorResult = getEgameAnchor(queryAnchor)
        return if (anchorResult != null) {
            val html = getHtml(queryAnchor)
            val title = html?.let { it1 -> TextUtil.subString(it1, "title:\"", "\",") }
            val coverPic = html?.let { it1 -> TextUtil.subString(it1, "\"coverpic\":\"", "\",") }
            queryAnchor.apply {
                status = anchorResult.data.key.retBody.data.isLive == 1
                this.title = title
                avatar = anchorResult.data.key.retBody.data.faceUrl
                keyFrame = coverPic
                typeName = anchorResult.data.key.retBody.data.appname
            }
            true
        } else false
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true
    override fun updateAnchorsDataByCookie(queryList: List<Anchor>): ResultUpdateAnchorByCookie {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return super.updateAnchorsDataByCookie(queryList)
            val list = egameqqService.getFollowList(getCookie()).execute().body()
            list?.let { followList ->
                if (followList.data.key.retCode != 0)
                    return ResultUpdateAnchorByCookie(false, followList.data.key.retMsg)
                val follows = list.data.key.retBody.data.online_follow_list
                val failedList = mutableListOf<Anchor>().also {
                    it.addAll(queryList)
                }
                queryList.forEach goOn@{ anchor ->
                    follows.forEach {
                        if (anchor.roomId == it.live_info.anchor_id.toString()) {
                            anchor.apply {
                                status = it.status == 1
                                title = it.live_info.title
                                avatar = it.live_info.anchor_face_url
                                keyFrame = it.live_info.video_info.url
                                typeName = it.live_info.appname
                                online = AnchorUtil.formatOnlineNumber(it.live_info.online)
                            }
                            failedList.remove(anchor)
                            return@forEach
                        }
                    }
                }
                failedList.setHintWhenFollowListDidNotContainsTheAnchor()
                return ResultUpdateAnchorByCookie(true)
            }
        }
        return super.updateAnchorsDataByCookie(queryList)
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val html = getHtml(queryAnchor)
        html?.let {
            val jsonStr = TextUtil.subString(html, "playerInfo = ", ";window._playerInfo")
            val playerInfo = Gson().fromJson<PlayerInfo>(jsonStr, PlayerInfo::class.java)
            playerInfo.urlArray.forEach {
                if (it.playUrl.isNotEmpty())
                    return it.playUrl
            }
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

    @Suppress("DeferredResultUnused")
    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = egameqqService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        if (result != null)
            result.apply {
                val document = Jsoup.parse(this)
                val anchors = document.getElementsByClass("gui-list-anchor")
                runBlocking {
                    anchors.forEachIndexed { index, element ->
                        if (index >= 5)
                            return@runBlocking
                        async(Dispatchers.IO) {
                            val id = element.getElementsByTag("a").attr("href").replace("/", "")
                            getAnchor(id)?.let { list.add(it) }
                        }
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
        if (list?.data?.key?.retCode != 0)
            return ResultGetAnchorListByCookieMode(
                false,
                null,
                list?.data?.key?.retMsg.toString()
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
                            liveTime = TimeUtil.timeStampToString(it.last_play_time)
                        )
                    )
                }
            }
            return ResultGetAnchorListByCookieMode(
                true,
                anchorList
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
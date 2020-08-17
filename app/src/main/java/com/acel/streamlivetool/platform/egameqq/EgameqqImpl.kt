package com.acel.streamlivetool.platform.egameqq

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.egameqq.bean.EgameQQAnchor
import com.acel.streamlivetool.platform.egameqq.bean.Param
import com.acel.streamlivetool.platform.egameqq.bean.PlayerInfo
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson
import kotlinx.coroutines.*
import org.jsoup.Jsoup


class EgameqqImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            EgameqqImpl()
        }
    }

    override val platform: String = "egameqq"
    override val platformShowNameRes: Int = R.string.egameqq
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
                    return Anchor(
                        platform,
                        it.data.key.retBody.data.nickName,
                        it.data.key.retBody.data.aliasId.toString(),
                        it.data.key.retBody.data.uid.toString()
                    )
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

    override fun getAnchorAttribute(queryAnchor: Anchor): AnchorAttribute? {
        val anchor = getEgameAnchor(queryAnchor)

        anchor?.let { anchorX ->
            val html = getHtml(queryAnchor)
            val title = html?.let { it1 -> TextUtil.subString(it1, "title:\"", "\",") }
            val coverPic = html?.let { it1 -> TextUtil.subString(it1, "\"coverpic\":\"", "\",") }

            return title?.let { titleX ->
                AnchorAttribute(
                    queryAnchor,
                    anchorX.data.key.retBody.data
                        .isLive == 1,
                    titleX,
                    anchor.data.key.retBody.data.faceUrl,
                    coverPic,
                    typeName = anchor.data.key.retBody.data.appname
                )
            }
        }
        return null
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
                            return@forEachIndexed
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

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        if (readCookie().isEmpty())
            return super.getAnchorsWithCookieMode()
        val list = egameqqService.getFollowList(readCookie()).execute().body()
        if (list?.data?.key?.retCode != 0)
            return AnchorsCookieMode(false, null, list?.data?.key?.retMsg.toString())
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
                            typeName = it.live_info.appname
                        )
                    )
                }
            }
            return AnchorsCookieMode(true, anchorList)
        }
    }

    override fun getLoginUrl(): String {
        return "https://egame.qq.com/usercenter/followlist"
    }

    override fun checkLoginOk(cookie: String): Boolean {
        return cookie.contains("pgg_uid") && cookie.contains("pgg_access_token")
    }

    override fun usePcAgent(): Boolean {
        return true
    }
}
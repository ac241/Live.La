package com.acel.streamlivetool.platform.egameqq

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.egameqq.bean.EgameQQAnchor
import com.acel.streamlivetool.platform.egameqq.bean.Param
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson


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
            val roomId = tempRoomId?.split("_")?.get(1)
            queryAnchor.roomId = roomId.toString()
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
                    queryAnchor.platform,
                    queryAnchor.roomId,
                    anchorX.data.key.retBody.data
                        .isLive == 1,
                    titleX,
                    anchor.data.key.retBody.data.faceUrl,
                    coverPic
                )
            }
        }
        return null
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val html = getHtml(queryAnchor)
        html?.let {
            return TextUtil.subString(html, "\"playUrl\":\"", "\",")
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

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        val list = egameqqService.getFollowList(readCookie()).execute().body()
        if (list != null) {
            val anchorList = mutableListOf<AnchorsCookieMode.Anchor>()
            list.data.key.retBody.data.online_follow_list.let { follows ->
                follows.forEach {
                    anchorList.add(
                        AnchorsCookieMode.Anchor(it.status == 1, it.live_info.title)
                            .also { anchor ->
                                anchor.nickname = it.live_info.anchor_name
                                anchor.platform = platform
                                anchor.showId = it.live_info.anchor_id.toString()
                                anchor.roomId = it.live_info.anchor_id.toString()
                            }
                    )
                }
            }
            return AnchorsCookieMode(true, anchorList)
        }
        return super.getAnchorsWithCookieMode()
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
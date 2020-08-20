package com.acel.streamlivetool.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.platform.huya.bean.Subscribe
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.UnicodeUtil
import java.net.URLEncoder


class HuyaImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            HuyaImpl()
        }
    }

    override val platform: String = "huya"
    override val platformShowNameRes: Int = R.string.huya
    override val supportCookieMode: Boolean = true
    private val huyaService: HuyaApi = retrofit.create(HuyaApi::class.java)

    private fun getHtml(queryAnchor: Anchor): String? {
        return huyaService.getHtml(queryAnchor.showId).execute().body()
    }

    private fun getMHtml(queryAnchor: Anchor): String? {
        return huyaService.getMHtml(queryAnchor.showId).execute().body()
    }

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val html: String? = getMHtml(queryAnchor)
        html?.let {
            val showId = TextUtil.subString(it, "class=\"roomid\">房间号 : ", "</h2>")
            if (showId != null && showId.isNotEmpty()) {
                val nickname =
                    TextUtil.subString(it, "ANTHOR_NICK = '", "';")
                        ?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                val uid = TextUtil.subString(it, "ayyuid: '", "',")
                return Anchor(platform, nickname.toString(), showId, uid.toString())
            }
        }
        return null
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val html: String? = getMHtml(queryAnchor)
        return if (html != null) {
            queryAnchor.apply {
                val tempStatus = TextUtil.subString(html, "ISLIVE =", ";")?.trim() == "true"
                status = tempStatus
                title = TextUtil.subStringAfterWhat(
                    html,
                    "class=\"live-info-desc\"",
                    "<h1>",
                    "</h1>"
                ) ?: "获取标题失败"
                avatar = TextUtil.subStringAfterWhat(
                    html,
                    "class=\"live-info-img\"",
                    "<img src=\"",
                    "\""
                )
                typeName = TextUtil.subString(html, "<span class=\"title\">", "</span>")

                if (!tempStatus)
                    getHtml(queryAnchor)?.let { screenShotHtml ->
                        keyFrame = TextUtil.subString(screenShotHtml, "\"screenshot\":\"", "\",")
                            ?.replace("\\", "")
                    }
            }
            true
        } else false
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true
    override fun updateAnchorsDataByCookie(queryList: List<Anchor>): ResultUpdateAnchorByCookie {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return super.updateAnchorsDataByCookie(queryList)
            val subscribe = getSubscribe(cookie)
            subscribe?.let { sub ->
                if (sub.status != 1000L)
                    return ResultUpdateAnchorByCookie(false, sub.message)
                val failedList = mutableListOf<Anchor>().also { it.addAll(queryList) }
                subscribe.result.list.forEach { subAnchor ->
                    queryList.forEach goOn@{ anchor ->
                        if (subAnchor.uid.toString() == anchor.roomId) {
                            anchor.apply {
                                status = subAnchor.isLive
                                title = subAnchor.intro
                                avatar = subAnchor.avatar180
                                keyFrame = subAnchor.screenshot
                                typeName = subAnchor.gameName
                            }
                            failedList.remove(anchor)
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
        val html = getMHtml(queryAnchor)
        html?.let {
            val streamStr = TextUtil.subString(it, "liveLineUrl = \"", "\";")
            if (streamStr != null)
                return "https:$streamStr"
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri =
            Uri.parse(
                "yykiwi://homepage?banneraction=" + URLEncoder.encode(
                    "https://secstatic.yy.com/huya?hyaction=live&uid=${anchor.roomId}",
                    "utf-8"
                )
            )
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_BROWSABLE)
        intent.action = Intent.ACTION_VIEW
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = huyaService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = result.response.`1`.docs
            resultList.forEach {
                list.add(Anchor(platform, it.game_nick, it.room_id.toString(), it.uid.toString()))
            }
        }
        return list
    }

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        getCookie().run {
            if (this.isEmpty())
                return super.getAnchorsWithCookieMode()
            else {
                val subscribe = getSubscribe(this)
                if (subscribe?.status != 1000L)
                    return AnchorsCookieMode(
                        false,
                        null,
                        subscribe?.message.toString()
                    )
                else
                    return run {
                        val list = mutableListOf<Anchor>()
                        subscribe.result.list.forEach {
                            list.add(
                                Anchor(
                                    platform = platform,
                                    nickname = it.nick,
                                    showId = it.profileRoom.toString(),
                                    roomId = it.uid.toString(),
                                    status = it.isLive,
                                    title = it.intro,
                                    avatar = it.avatar180,
                                    keyFrame = it.screenshot,
                                    typeName = it.gameName
                                )
                            )
                        }
                        AnchorsCookieMode(
                            true,
                            list
                        )
                    }
            }
        }
    }

    private fun getSubscribe(cookie: String): Subscribe? {
        val cs = cookie.split(";")
        var uid = ""
        cs.forEach {
            if (it.contains("yyuid")) {
                val yyuid = it.split("=")
                uid = yyuid[1]
            }
        }
        return huyaService.getSubscribe(cookie, uid).execute().body()
    }

    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("udb_biztoken") && cookie.contains("udb_passport"))
            return true
        return false
    }

    override fun usePcAgent(): Boolean {
        return true
    }

    override fun getLoginUrl(): String {
        return "https://www.huya.com/myfollow"
    }
}
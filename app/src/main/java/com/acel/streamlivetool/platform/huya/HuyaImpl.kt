package com.acel.streamlivetool.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.AnchorAttribute
import com.acel.streamlivetool.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.huya.bean.Stream
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.UnicodeUtil
import com.google.gson.Gson
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

    private fun getAnchorBackup1(queryAnchor: Anchor): Anchor? {
        val html: String? = getHtml(queryAnchor)
        html?.let {
            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
            if (showId != null && showId.isNotEmpty()) {
                val nickname =
                    TextUtil.subString(it, "\"nick\":\"", "\",")
                        ?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                val uid = TextUtil.subString(it, "\"lp\":", ",")?.replace("\"", "")
                return Anchor(platform, nickname.toString(), showId, uid.toString())
            }
        }
        return null
    }

    override fun getAnchorAttribute(queryAnchor: Anchor): AnchorAttribute? {
        val html: String? = getMHtml(queryAnchor)
        html?.let {
            //            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
            val state = TextUtil.subString(it, "ISLIVE =", ";")?.trim() == "true"
            val title = TextUtil.subStringAfterWhat(
                it,
                "class=\"live-info-desc\"",
                "<h1>",
                "</h1>"
            ) ?: "获取标题失败"
            val avatar = TextUtil.subStringAfterWhat(
                it,
                "class=\"live-info-img\"",
                "<img src=\"",
                "\""
            )
            if (!state) {
                return AnchorAttribute(
                    queryAnchor.platform,
                    queryAnchor.roomId,
                    state,
                    title,
                    avatar
                )
            } else {
                getHtml(queryAnchor)?.let {
                    val screenshot =
                        TextUtil.subString(it, "\"screenshot\":\"", "\",")?.replace("\\", "")
                    return AnchorAttribute(
                        queryAnchor.platform,
                        queryAnchor.roomId,
                        state,
                        title,
                        avatar,
                        screenshot
                    )
                }
            }
        }
        return null
    }

    fun getAnchorAttributeBackup1(queryAnchor: Anchor): AnchorAttribute? {
        val html: String? = getHtml(queryAnchor)
        html?.let {
            //            val showId = TextUtil.subString(it, "\"profileRoom\":\"", "\",")
            val state = TextUtil.subString(it, "\"state\":\"", "\",")
            val title = TextUtil.subString(it, "\"introduction\":\"", "\",")
            val avatar = TextUtil.subStringAfterWhat(it, "TT_META_DATA", "\"avatar\":\"", "\",")
                ?.replace("\\", "")
            val screenshot = TextUtil.subString(it, "\"screenshot\":\"", "\",")?.replace("\\", "")
            if (state != null && title != null && state.isNotEmpty())
                return AnchorAttribute(
                    queryAnchor.platform,
                    queryAnchor.roomId,
                    state == "ON",
                    UnicodeUtil.decodeUnicode(title),
                    avatar,
                    if (screenshot != null && screenshot.isNotEmpty()) screenshot else null
                )
        }
        return null
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

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        readCookie().run {
            if (this.isEmpty())
                return super.getAnchorsWithCookieMode()
            else {
                val cs = this.split(";")
                var uid = ""
                cs.forEach {
                    if (it.contains("yyuid")) {
                        val yyuid = it.split("=")
                        uid = yyuid[1]
                    }
                }
                val subscribe = huyaService.getSubscribe(this, uid).execute().body()
                return if (subscribe != null) {
                    val list = mutableListOf<AnchorsCookieMode.Anchor>()
                    subscribe.result.list.forEach {
                        list.add(
                            AnchorsCookieMode.Anchor(
                                it.isLive,
                                it.intro,
                                it.avatar180,
                                it.screenshot
                            )
                                .also { anchor ->
                                    anchor.platform = platform
                                    anchor.nickname = it.nick
                                    anchor.roomId = it.uid.toString()
                                    anchor.showId = it.profileRoom.toString()
                                })
                    }
                    AnchorsCookieMode(true, list)
                } else
                    super.getAnchorsWithCookieMode()
            }
        }
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
package com.acel.streamlivetool.platform.huya

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Result
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.platform.huya.HuyaImpl.PatternUtil.getMatchString
import com.acel.streamlivetool.platform.huya.bean.Subscribe
import com.acel.streamlivetool.util.*
import java.net.URLEncoder
import java.util.regex.Pattern


class HuyaImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            HuyaImpl()
        }
    }

    override val platform: String = "huya"
    override val platformShowNameRes: Int = R.string.huya
    override val iconRes: Int = R.drawable.ic_huya
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
            val showId = html.getMatchString(PatternUtil.showId)
            if (showId != null && showId.isNotEmpty()) {
                val nickname = html.getMatchString(PatternUtil.nickName)
                    ?.let { it1 -> UnicodeUtil.decodeUnicode(it1) }
                var avatarUrl = TextUtil.subString(html, "<span class=\"pic-clip\">", "alt=\"")
                    ?.getMatchString(PatternUtil.avatar)
                avatarUrl?.let {
                    if (!it.contains("https:"))
                        avatarUrl = "https:$avatarUrl"
                }
                return Anchor(
                    platform = platform,
                    nickname = nickname.toString(),
                    showId = showId,
                    roomId = html.getMatchString(PatternUtil.uid).toString(),
                    status = html.getMatchString(PatternUtil.status) == "true",
                    title = html.getMatchString(PatternUtil.title),
                    avatar = avatarUrl,
                    keyFrame = html.getMatchString(PatternUtil.keyFrame),
                    typeName = html.getMatchString(PatternUtil.typeName),
                    online = html.getMatchString(PatternUtil.online)
                )
            }
        }
        return null
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val anchor = getAnchor(queryAnchor)
        return if (anchor != null) {
            queryAnchor.apply {
                status = anchor.status
                title = anchor.title
                avatar = anchor.avatar
                typeName = anchor.typeName
                keyFrame = anchor.keyFrame
                online = anchor.online
            }
            true
        } else
            false
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true

    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
        val html = getMHtml(queryAnchor)
        html?.let {
            val streamStr = TextUtil.subString(it, "liveLineUrl = \"", "\";")
            if (streamStr != null)
                return StreamingLive(url = "https:$streamStr", null, null)
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
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.game_nick,
                        showId = it.room_id.toString(),
                        roomId = it.uid.toString(),
                        status = it.gameLiveOn,
                        avatar = it.game_avatarUrl52
                    )
                )
            }
        }
        return list
    }

    override fun getAnchorsByCookieMode(): ResultGetAnchorListByCookieMode {
        getCookie().run {
            if (this.isEmpty())
                return super.getAnchorsByCookieMode()
            else {
                val subscribe = getSubscribe(this)
                if (subscribe?.status != 1000L)
                    return ResultGetAnchorListByCookieMode(
                        success = false,
                        isCookieValid = false,
                        anchorList = null,
                        message = subscribe?.message.toString()
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
                                    typeName = it.gameName,
                                    online = AnchorUtil.formatOnlineNumber(it.totalCount.toInt()),
                                    liveTime = TimeUtil.timestampToString(it.startTime)
                                )
                            )
                        }
                        ResultGetAnchorListByCookieMode(
                            success = true,
                            isCookieValid = true,
                            anchorList = list
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

    override fun loginWithPcAgent(): Boolean = true

    override val loginTips: String
        get() = "虎牙的cookie有效期约为7天"

    override fun getLoginUrl(): String {
        return "https://www.huya.com/myfollow"
    }

    override val supportFollow: Boolean = true
    override fun follow(anchor: Anchor): Result {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return Result(false, "未登录")
            val uid = CookieUtil.getCookieField(cookie, "yyuid")
            uid?.let { u ->
                val response =
                    huyaService.follow(cookie, anchor.roomId, u, System.currentTimeMillis())
                        .execute().body()
                response?.apply {
                    return if (status == 1)
                        Result(true, "关注成功")
                    else
                        Result(false, message)
                }
            }
        }
        return Result(false, "发生错误")
    }

    private object PatternUtil {
        val showId: Pattern = Pattern.compile("<h2 class=\"roomid\">房间号 : (.*?)</h2>")
        val nickName: Pattern = Pattern.compile("ANTHOR_NICK = '(.*?)';")
        val uid: Pattern = Pattern.compile("ayyuid: '(.*?)',")
        val typeName: Pattern = Pattern.compile("gameName = '(.*?)'")
        val keyFrame: Pattern = Pattern.compile("picURL = '(.*?)'")
        val title: Pattern = Pattern.compile("liveRoomName = '(.*?)'")
        val status: Pattern = Pattern.compile("ISLIVE = (.*?);")
        val online: Pattern = Pattern.compile("liveTotalCount = '(.*?)'")
        val avatar: Pattern = Pattern.compile("<img src=\"(.*?)\"")


        fun String.getMatchString(pattern: Pattern): String? {
            val matcher = pattern.matcher(this)
            return if (matcher.find())
                matcher.group(1)
            else null
        }
    }

//    override val danmuManager: IPlatform.DanmuManager = HuyaDanmuManager()
}
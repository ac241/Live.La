package com.acel.streamlivetool.platform.douyu

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.platform.douyu.bean.Followed
import com.acel.streamlivetool.platform.douyu.bean.LiveInfo
import com.acel.streamlivetool.platform.douyu.bean.LiveInfoTestError
import com.acel.streamlivetool.platform.douyu.bean.RoomInfo
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.CookieUtil
import com.acel.streamlivetool.util.TextUtil
import com.acel.streamlivetool.util.TimeUtil
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import java.util.*


class DouyuImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            DouyuImpl()
        }
    }

    override val platform: String = "douyu"
    override val platformShowNameRes: Int = R.string.douyu
    override val iconRes: Int = R.drawable.ic_douyu
    override val supportCookieMode: Boolean = true
    private val douyuService: DouyuApi = retrofit.create(DouyuApi::class.java)
    override val danmuClient: IPlatform.DanmuClient = DouyuDanmuClient()

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val msg = douyuService.getRoomInfoMsg(queryAnchor.showId).execute().body()
        //error不为0从html获取
        return if (msg?.error != 0) {
            getAnchorFromHtml(queryAnchor)
        } else {
            val roomInfo: RoomInfo? =
                douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
            val info = roomInfo?.data
            if (info == null)
                null
            else {
                Anchor(
                    platform = platform,
                    nickname = info.ownerName,
                    showId = info.roomId,
                    roomId = info.roomId,
                    status = info.roomStatus == "1",
                    title = info.roomName,
                    avatar = info.avatar,
                    keyFrame = info.roomThumb,
                    typeName = info.cateName,
                    online = info.online.toString(),
                    liveTime = info.startTime
                )
            }
        }
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        return runBlocking {
            val info = async(Dispatchers.IO) {
                val roomInfo =
                    douyuService.getRoomInfoBetard(queryAnchor.showId).execute().body()
                if (roomInfo != null) {
                    queryAnchor.apply {
                        status = roomInfo.room.show_status == 1
                        title = roomInfo.room.room_name
                        avatar = roomInfo.room.avatar.big
                        keyFrame = roomInfo.room.room_pic
                        if (roomInfo.room.videoLoop == 1) secondaryStatus =
                            MyApplication.application.getString(R.string.video_looping)
                        typeName = roomInfo.game.tag_name
                    }
                    true
                } else false
            }
            val online = async(Dispatchers.IO) {
                val roomInfo = douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
                if (roomInfo?.data?.online != null) {
                    queryAnchor.online = AnchorUtil.formatOnlineNumber(roomInfo.data.online)
                    true
                } else false
            }
            info.await() && online.await()
        }
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true

    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
        val h5Enc = douyuService.getH5Enc(queryAnchor.roomId).execute().body()
        if (h5Enc?.error == 0) {
            val enc = h5Enc.data["room" + queryAnchor.roomId].toString()
            val paramsMap = getRequestParams(enc, queryAnchor, queryQuality?.num ?: 4)
            paramsMap?.let { pm ->
                val jsonStr = douyuService.getLiveInfo(queryAnchor.roomId, pm).execute().body()
                val gson = Gson()
                val testError = gson.fromJson(jsonStr, LiveInfoTestError::class.java)
                if (testError.error == 0) {
                    val liveInfo = gson.fromJson(jsonStr, LiveInfo::class.java)
                    liveInfo?.data?.let { data ->
                        val multiRates = data.multirates
                        val returnQualityList = mutableListOf<StreamingLive.Quality>()
                        multiRates.forEach {
                            returnQualityList.add(StreamingLive.Quality(it.name, it.rate))
                        }
                        val rate = liveInfo.data.rate
                        val rateIndex = multiRates.indexOf(LiveInfo.Multirate(0, 0, "", rate))
                        val currentQuality =
                            if (rateIndex != -1) multiRates[rateIndex] else null
                        val returnCurrentQuality =
                            if (currentQuality != null) StreamingLive.Quality(
                                currentQuality.name,
                                currentQuality.rate
                            ) else null

                        val returnUrl: String =
                            liveInfo.data.rtmp_url + "/" + liveInfo.data.rtmp_live

                        return StreamingLive(
                            url = returnUrl,
                            currentQuality = returnCurrentQuality,
                            qualityList = returnQualityList
                        )
                    }
                }
            }
        }
        return null
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("douyutvtest://?type=4&room_id=${anchor.roomId}")
        intent.data = uri
        intent.flags = FLAG_ACTIVITY_NEW_TASK
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = douyuService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = this.data.roomResult
            resultList.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.nickName,
                        showId = it.rid.toString(),
                        roomId = it.rid.toString(),
                        status = it.isLive == 1,
                        avatar = it.avatar
                    )
                )
            }
        }
        return list
    }

    private fun getAnchorFromHtml(queryAnchor: Anchor): Anchor? {
        val html = douyuService.getRoomInfo(queryAnchor.showId).execute().body().toString()
        val nickname = TextUtil.subString(html, "\"nickname\":\"", "\",")
        val showId = TextUtil.subString(html, "\"rid\":", ",\"")
        return if (nickname != null && showId != null && showId.isNotEmpty())
            Anchor(platform, nickname, showId, showId)
        else
            null
    }

    private fun getRequestParams(
        enc: String,
        anchor: Anchor,
        rate: Int = 4
    ): MutableMap<String, String>? {
        val context = org.mozilla.javascript.Context.enter()
        val uuid = UUID.randomUUID().toString().replace("-", "")
//        val uuid = "07095540bc131c2cc23726a200021501"
        val time = (Date().time / 1000).toString()
        val inputStream = MyApplication.application.resources.openRawResource(R.raw.douyu_crypto_js)
        val cryptoJs = inputStream.bufferedReader().use {
            it.readText()
        }
        inputStream.close()
        try {
            val scope = context.initStandardObjects()
            context.optimizationLevel = -1
            context.evaluateString(scope, cryptoJs, "cryptoJs", 1, null)
            context.evaluateString(scope, enc, "enc", 1, null)
            val result =
                context.evaluateString(
                    scope,
                    "ub98484234(${anchor.roomId},\"${uuid}\",${time})",
                    "douyu",
                    1,
                    null
                )
            val params = org.mozilla.javascript.Context.toString(result)
            val list = params.split("&")
            val map = mutableMapOf<String, String>()
            list.forEach {
                val paramList = it.split("=")
                map[paramList[0]] = paramList[1]
            }
            map["ver"] = "Douyu_219041925"
            map["rate"] = "$rate"
            map["iar"] = "1"
            map["ive"] = "0"
            map["cdn"] = ""
            return map
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            org.mozilla.javascript.Context.exit()
        }
        return null
    }

    override fun getAnchorsByCookieMode(): ResultGetAnchorListByCookieMode {
        fun addToList(list: MutableList<Anchor>, followed: Followed) {
            followed.data.list.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.nickname,
                        showId = it.room_id.toString(),
                        roomId = it.room_id.toString(),
                        status = it.show_status == 1,
                        title = it.room_name,
                        avatar = it.avatar_small,
                        keyFrame = it.room_src,
                        secondaryStatus = if (it.videoLoop == 1) MyApplication.application.getString(
                            R.string.video_looping
                        ) else null,
                        typeName = it.game_name,
                        online = it.online,
                        liveTime = TimeUtil.timestampToString(it.show_time)
                    )
                )
            }
        }

        getCookie().run {
            if (this.isEmpty())
                return super.getAnchorsByCookieMode()
            else {
                val followed = douyuService.getFollowed(this).execute().body()
                if (followed?.error != 0)
                    return ResultGetAnchorListByCookieMode(
                        success = false,
                        isCookieValid = false,
                        anchorList = null,
                        message = followed?.msg.toString()
                    )
                else {
                    val list = mutableListOf<Anchor>()
                    addToList(list, followed)
                    //如果页数大于1
                    followed.data.pageCount.let { page ->
                        if (page > 1) {
                            runBlocking {
                                for (i in 2..followed.data.pageCount) {
                                    async(Dispatchers.IO) {
                                        val followedNext =
                                            douyuService.getFollowed(this@run, i).execute().body()
                                        if (followedNext != null) {
                                            addToList(list, followedNext)
                                        }
                                    }.start()
                                }
                            }
                        }
                    }
                    return ResultGetAnchorListByCookieMode(
                        success = true,
                        isCookieValid = true,
                        anchorList = list
                    )
                }
            }
        }
    }


    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("PHPSESSID") && cookie.contains("dy_auth"))
            return true
        return false
    }

    override fun loginWithPcAgent(): Boolean = true

    override val loginTips: String
        get() = "斗鱼的cookie有效期约为7天，昵称登录可能无法使用。"

    override fun getLoginUrl(): String {
        return "https://passport.douyu.com/index/login"
    }

    override fun follow(anchor: Anchor): Pair<Boolean, String> {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return Pair(false, "未登录")
            val response = douyuService.initCsrf(cookie).execute()
            val setCookie = response.headers().get("Set-Cookie") ?: ""
            val ctn = CookieUtil.getCookieField(setCookie, "acf_ccn")
            ctn?.let { c ->
                val result =
                    douyuService.follow("$setCookie;$cookie", anchor.roomId, c).execute().body()
                result?.apply {
                    return if (error == 0)
                        Pair(true, "关注成功")
                    else
                        Pair(false, msg)
                }
            }
        }
        return Pair(false, "发生错误")
    }
}
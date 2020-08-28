package com.acel.streamlivetool.platform.douyu

import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_NEW_TASK
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.base.MyApplication
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.AnchorsCookieMode
import com.acel.streamlivetool.platform.bean.ResultUpdateAnchorByCookie
import com.acel.streamlivetool.platform.douyu.bean.LiveInfo
import com.acel.streamlivetool.platform.douyu.bean.LiveInfoTestError
import com.acel.streamlivetool.platform.douyu.bean.RoomInfo
import com.acel.streamlivetool.util.TextUtil
import com.google.gson.Gson
import java.util.*


class DouyuImpl : IPlatform {
    companion object {
        val INSTANCE by lazy {
            DouyuImpl()
        }
    }

    override val platform: String = "douyu"
    override val platformShowNameRes: Int = R.string.douyu
    override val supportCookieMode: Boolean = true
    private val douyuService: DouyuApi = retrofit.create(DouyuApi::class.java)

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
//        return getAnchorFromHtml()
        val msg = douyuService.getRoomInfoMsg(queryAnchor.showId).execute().body()
        //error不为0从html获取
        return if (msg?.error != 0) {
            getAnchorFromHtml(queryAnchor)
        } else {
            val roomInfo: RoomInfo? =
                douyuService.getRoomInfoFromOpen(queryAnchor.showId).execute().body()
            val roomId = roomInfo?.data?.roomId
            val ownerName = roomInfo?.data?.ownerName
            Anchor(platform, ownerName.toString(), roomId.toString(), roomId.toString())
        }
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        val roomInfo =
            douyuService.getRoomInfoBetard(queryAnchor.showId).execute().body()
        return if (roomInfo != null) {
            queryAnchor.apply {
                status = roomInfo.room.show_status == 1
                title = roomInfo.room.room_name
                avatar = roomInfo.room.avatar.big
                keyFrame = roomInfo.room.room_pic
                if (roomInfo.room.videoLoop == 1) secondaryStatus = MyApplication.application.getString(R.string.video_looping)
                typeName = roomInfo.game.tag_name
            }
            true
        } else false
    }

    override fun supportUpdateAnchorsByCookie(): Boolean = true

    override fun updateAnchorsDataByCookie(queryList: List<Anchor>): ResultUpdateAnchorByCookie {
        getCookie().run {
            if (this.isEmpty())
                return super.updateAnchorsDataByCookie(queryList)
            else {
                val followed = douyuService.getFollowed(this).execute().body()
                followed?.apply {
                    if (followed.error != 0)
                        return ResultUpdateAnchorByCookie(
                            false,
                            followed.msg
                        )
                    val failedList = mutableListOf<Anchor>().also {
                        it.addAll(queryList)
                    }
                    queryList.forEach goOn@{ anchor ->
                        followed.data.list.forEach { anchorX ->
                            if (anchor.roomId == anchorX.room_id.toString()) {
                                anchor.apply {
                                    status = anchorX.show_status == 1
                                    title = anchorX.room_name
                                    avatar = anchorX.avatar_small
                                    keyFrame = anchorX.room_src
                                    secondaryStatus = if (anchorX.videoLoop == 1) MyApplication.application.getString(R.string.video_looping) else null
                                    typeName = anchorX.game_name
                                }
                                failedList.remove(anchor)
                                return@goOn
                            }
                        }
                    }
                    failedList.setHintWhenFollowListDidNotContainsTheAnchor()
                    return ResultUpdateAnchorByCookie(true)
                }
                return super.updateAnchorsDataByCookie(queryList)
            }
        }
    }

    override fun getStreamingLiveUrl(queryAnchor: Anchor): String? {
        val h5Enc = douyuService.getH5Enc(queryAnchor.roomId).execute().body()
        if (h5Enc?.error == 0) {
            val enc = h5Enc.data["room" + queryAnchor.roomId].toString()
            val paramsMap = getRequestParams(enc, queryAnchor)
            paramsMap?.let {
                val jsonStr = douyuService.getLiveInfo(queryAnchor.roomId, it).execute().body()
                val gson = Gson()
                val testError = gson.fromJson(jsonStr, LiveInfoTestError::class.java)
                if (testError.error == 0) {
                    val liveInfo = gson.fromJson(jsonStr, LiveInfo::class.java)
                    return liveInfo?.data?.rtmpUrl + "/" + liveInfo?.data?.rtmpLive
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
                list.add(Anchor(platform, it.nickName, it.rid.toString(), it.rid.toString()))
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

    private fun getRequestParams(enc: String, anchor: Anchor): MutableMap<String, String>? {
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
            map["rate"] = "0"
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

    override fun getAnchorsWithCookieMode(): AnchorsCookieMode {
        getCookie().run {
            if (this.isEmpty())
                return super.getAnchorsWithCookieMode()
            else {
                val followed = douyuService.getFollowed(this).execute().body()
                if (followed?.error != 0)
                    return AnchorsCookieMode(
                        false,
                        null,
                        followed?.msg.toString()
                    )
                else
                    return run {
                        val list = mutableListOf<Anchor>()
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
                                    secondaryStatus = if (it.videoLoop == 1) MyApplication.application.getString(R.string.video_looping) else null,
                                    typeName = it.game_name
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

    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("PHPSESSID") && cookie.contains("dy_auth"))
            return true
        return false
    }

    override fun getLoginUrl(): String {
        return "https://passport.douyu.com/index/login"
    }
}
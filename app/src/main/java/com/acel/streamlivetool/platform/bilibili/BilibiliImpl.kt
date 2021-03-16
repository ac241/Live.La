package com.acel.streamlivetool.platform.bilibili

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.acel.streamlivetool.R
import com.acel.streamlivetool.bean.Anchor
import com.acel.streamlivetool.bean.Result
import com.acel.streamlivetool.bean.StreamingLive
import com.acel.streamlivetool.platform.IPlatform
import com.acel.streamlivetool.platform.bean.ResultGetAnchorListByCookieMode
import com.acel.streamlivetool.platform.bilibili.bean.RoomPlayInfo
import com.acel.streamlivetool.util.AnchorUtil
import com.acel.streamlivetool.util.CookieUtil.getCookieField
import com.acel.streamlivetool.util.TimeUtil
import kotlinx.coroutines.*


class BilibiliImpl : IPlatform {
    companion object {
        val INSTANCE by lazy { BilibiliImpl() }
    }

    override val platform: String = "bilibili"
    override val platformShowNameRes: Int = R.string.bilibili
    override val iconRes: Int = R.drawable.ic_bilibili
    override val supportCookieMode: Boolean = true
    private val bilibiliService: BilibiliApi = retrofit.create(BilibiliApi::class.java)
    override val danmuClient: IPlatform.DanmuClient? = BilibiliDanmuClient()

    override fun getAnchor(queryAnchor: Anchor): Anchor? {
        val anchor = Anchor()
        var success = true
        runBlocking {
            val result = async(Dispatchers.IO) {
                bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()
            }
            val h5Info = async(Dispatchers.IO) {
                bilibiliService.getH5InfoByRoom(queryAnchor.showId.toLong()).execute().body()
            }
            result.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0 && info != null) {
                        val roomId = info.room_id
                        val ownerName = getAnchorName(roomId.toLong())
                        anchor.apply {
                            platform = this@BilibiliImpl.platform
                            nickname = ownerName.toString()
                            showId = roomId.toString()
                            this.roomId = roomId.toString()
                            status = info.live_status == 1
                            title = info.title
                            keyFrame = info.keyframe
                            typeName = info.area_name
                            online = info.online.toString()
                            liveTime = info.live_time
                        }
                    } else {
                        success = false
                    }
                } else
                    success = false
            }
            h5Info.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0) {
                        anchor.avatar = info.anchor_info.base_info.face
                    } else
                        success = false
                } else
                    success = false
            }
        }
        return if (success) anchor else null
    }

    private fun getAnchorName(roomId: Long): String? {
        val h5Info =
            bilibiliService.getH5InfoByRoom(roomId).execute().body()
        return h5Info?.data?.anchor_info?.base_info?.uname
    }

    override fun updateAnchorData(queryAnchor: Anchor): Boolean {
        var success = true
        runBlocking {
            val result = async(Dispatchers.IO) {
                bilibiliService.getRoomInfo(queryAnchor.showId).execute().body()
            }
            val h5Info = async(Dispatchers.IO) {
                bilibiliService.getH5InfoByRoom(queryAnchor.showId.toLong()).execute().body()
            }
            result.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0 && info != null) {
                        queryAnchor.apply {
                            keyFrame = info.keyframe
                        }
                    } else {
                        success = false
                    }
                } else
                    success = false
            }
            h5Info.await().apply {
                if (this != null) {
                    val info = data
                    if (code == 0) {
                        queryAnchor.avatar = info.anchor_info.base_info.face
                        queryAnchor.apply {
                            status = data.room_info.live_status == 1
                            title = data.room_info.title
                            avatar = data.anchor_info.base_info.face
                            keyFrame = data.room_info.cover
                            typeName = data.room_info.area_name
                            online = AnchorUtil.formatOnlineNumber(data.room_info.online)
                        }
                    } else
                        success = false
                } else
                    success = false
            }
        }
        return success
    }

    override fun supportCookieMode(): Boolean = true


    override fun getStreamingLive(
        queryAnchor: Anchor,
        queryQuality: StreamingLive.Quality?
    ): StreamingLive? {
        //return 开头的为返回数据
        //info 开头的为获取到的数据
        val roomPlayInfo =
            bilibiliService.getRoomPlayInfo(queryAnchor.roomId, queryQuality?.num ?: 10000)
                .execute()
                .body()
        roomPlayInfo ?: return null
        if (roomPlayInfo.code != 0)
            return null

        val infoQualityList = roomPlayInfo.data.play_url.quality_description
        val returnQualityList = mutableListOf<StreamingLive.Quality>()
        infoQualityList.forEach {
            returnQualityList.add(StreamingLive.Quality(it.desc, it.qn))
        }
        //info 当前的质量
        val infoCurrentQuality = roomPlayInfo.data.play_url.current_qn
        val infoCurrentQualityDescription = infoQualityList[infoQualityList.indexOf(
            RoomPlayInfo.QualityDescription(
                "",
                infoCurrentQuality
            )
        )]

        //返回数据
        val returnUrl: String = roomPlayInfo.data.play_url.durl[0].url
        val returnCurrentQuality = StreamingLive.Quality(
            infoCurrentQualityDescription.desc,
            infoCurrentQualityDescription.qn
        )

        return StreamingLive(
            url = returnUrl,
            currentQuality = returnCurrentQuality,
            qualityList = returnQualityList
        )
    }

    override fun startApp(context: Context, anchor: Anchor) {
        val intent = Intent()
        val uri = Uri.parse("bilibili://live/${anchor.roomId}")
        intent.data = uri
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.action = "android.intent.action.VIEW"
        context.startActivity(intent)
    }

    override fun searchAnchor(keyword: String): List<Anchor>? {
        val result = bilibiliService.search(keyword).execute().body()
        val list = mutableListOf<Anchor>()
        result?.apply {
            val resultList = result.data.result
            resultList.forEach {
                list.add(
                    Anchor(
                        platform = platform,
                        nickname = it.uname.replace("<em class=\"keyword\">", "")
                            .replace("</em>", ""),
                        showId = it.roomid.toString(),
                        roomId = it.roomid.toString(),
                        status = it.is_live,
                        avatar = "http:${it.uface}"
                    )
                )
            }
        }
        return list
    }

    override fun getAnchorsByCookieMode(): ResultGetAnchorListByCookieMode {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return super.getAnchorsByCookieMode()
            var cookieOk = true
            var message = ""
            val anchorList = mutableListOf<Anchor>()
            runBlocking {
                val liveList = async(Dispatchers.IO) {
                    val result = bilibiliService.liveAnchor(cookie).execute().body()
                    result?.let {
                        val list = mutableListOf<Anchor>()
                        if (result.code != 0) {
                            cookieOk = false
                            message = result.message
                        }
                        if (result.data.total_count == 0)
                            return@async list
                        val rooms = result.data.rooms
                        rooms.forEach {
                            list.add(
                                Anchor(
                                    platform = platform,
                                    nickname = it.uname,
                                    showId = it.roomid.toString(),
                                    roomId = it.roomid.toString(),
                                    status = true,
                                    title = it.title,
                                    avatar = it.face,
                                    keyFrame = it.cover,
                                    typeName = it.area_v2_name,
                                    online = AnchorUtil.formatOnlineNumber(it.online),
                                    liveTime = TimeUtil.timestampToString(it.live_time)
                                )
                            )
                        }
                        list
                    }
                }
                val unLiveList = async(Dispatchers.IO) {
                    val result = bilibiliService.unLiveAnchor(cookie).execute().body()
                    val list = mutableListOf<Anchor>()
                    if (result?.data?.total_count == 0)
                        return@async list
                    val rooms = result?.data?.rooms
                    rooms?.forEach {
                        list.add(
                            Anchor(

                                platform = platform,
                                nickname = it.uname,
                                showId = it.roomid.toString(),
                                roomId = it.roomid.toString(),
                                status = false,
                                title = "${it.live_desc} 直播了 ${it.area_v2_name}",
                                avatar = it.face,
                                typeName = it.area_v2_name,
                                liveTime = it.live_desc
                            )
                        )
                    }
                    list
                }
                anchorList.addAll(liveList.await() as Collection<Anchor>)
                anchorList.addAll(unLiveList.await() as Collection<Anchor>)
            }
            return ResultGetAnchorListByCookieMode(true, cookieOk, anchorList, message)
        }
    }

    override fun checkLoginOk(cookie: String): Boolean {
        if (cookie.contains("SESSDATA") && cookie.contains("DedeUserID"))
            return true
        return false
    }

    override fun getLoginUrl(): String {
        return "https://passport.bilibili.com/login"
    }

    override val supportFollow: Boolean = true
    override fun follow(anchor: Anchor): Result {
        getCookie().let { cookie ->
            if (cookie.isEmpty())
                return Result(success = false, msg = "未登录")
            val roomInfo = bilibiliService.getRoomInfo(anchor.roomId).execute().body()
            roomInfo?.let { info ->
                info.data?.uid?.let { uid ->
                    val jct = getCookieField(cookie, "bili_jct")
                    jct?.let { j ->
                        val response = bilibiliService.follow(cookie, uid, j).execute().body()
                        response?.apply {
                            return if (code == 0)
                                Result(success = true, msg = "关注成功")
                            else
                                Result(success = false, msg = message)
                        }
                    }
                }

            }
        }
        return Result(success = true, msg = "发生错误")
    }
}